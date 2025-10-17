import numpy as np


def sample(texture, u, v):

    u = int(u * texture.shape[0])
    v = int((1-v) * texture.shape[1])
    return texture[v, u] / 255.0
    pass


class Fragment:
    def __init__(self, x: int, y: int, depth: float, interpolated_data):
        self.x = x
        self.y = y
        self.depth = depth
        self.interpolated_data = interpolated_data
        self.output = []


def edgeSide(p, v0, v1):
    return (p[0]-v0[0])*(v1[1]-v0[1]) - (p[1]-v0[1])*(v1[0]-v0[0])


class GraphicPipeline:
    def __init__(self, width, height):
        self.width = width
        self.height = height
        self.image = np.zeros((height, width, 3))
        self.depthBuffer = np.ones((height, width))
        self.shadowMap = np.ones((height, width))

    def VertexShader(self, vertex, data, lightData):
        outputVertex = np.zeros((18))

        x = vertex[0]
        y = vertex[1]
        z = vertex[2]
        w = 1.0

        vec = np.array([[x], [y], [z], [w]])

        # Transform to camera clip space
        vec = np.matmul(data['projMatrix'], np.matmul(data['viewMatrix'], vec))
        outputVertex[0] = vec[0] / vec[3]
        outputVertex[1] = vec[1] / vec[3]
        outputVertex[2] = vec[2] / vec[3]

        # Store normals and other vertex attributes
        outputVertex[3] = vertex[3]
        outputVertex[4] = vertex[4]
        outputVertex[5] = vertex[5]

        # Vector from camera to vertex
        outputVertex[6] = data['cameraPosition'][0] - vertex[0]
        outputVertex[7] = data['cameraPosition'][1] - vertex[1]
        outputVertex[8] = data['cameraPosition'][2] - vertex[2]

        # Vector from light to vertex
        outputVertex[9] = data['lightPosition'][0] - vertex[0]
        outputVertex[10] = data['lightPosition'][1] - vertex[1]
        outputVertex[11] = data['lightPosition'][2] - vertex[2]

        # Texture coordinates
        outputVertex[12] = vertex[6]
        outputVertex[13] = vertex[7]

        if (lightData is not None):
            # Transform vertex to light space
            lightVec = np.array([[x], [y], [z], [w]])
            lightVec = np.matmul(lightData['projMatrix'], np.matmul(
                lightData['viewMatrix'], lightVec))

            # Store light space position
            outputVertex[14] = lightVec[0]
            outputVertex[15] = lightVec[1]
            outputVertex[16] = lightVec[2]
            outputVertex[17] = lightVec[3]

        return outputVertex

    def generateShadowMap(self, vertices, triangles, lightData):
        # Clear shadow map
        self.shadowMap = np.ones((self.height, self.width))

        # Transform vertices to light space
        lightVertices = np.zeros((vertices.shape[0], 14))

        for i in range(vertices.shape[0]):
            # Use a simplified vertex shader for shadow map
            vertex = vertices[i]
            x, y, z = vertex[0], vertex[1], vertex[2]
            w = 1.0
            vec = np.array([[x], [y], [z], [w]])

            # Transform to light clip space
            vec = np.matmul(lightData['projMatrix'],
                            np.matmul(lightData['viewMatrix'], vec))

            lightVertices[i][0] = vec[0] / vec[3]  # x position
            lightVertices[i][1] = vec[1] / vec[3]  # y position
            lightVertices[i][2] = vec[2] / vec[3]  # z position (depth)

            # Copy remaining vertex attributes
            for j in range(3, min(14, len(vertex))):
                lightVertices[i][j] = vertex[j]

        # Rasterize triangles for shadow map
        shadowFragments = []
        for triangle in triangles:
            shadowFragments.extend(self.Rasterizer(
                lightVertices[triangle[0]],
                lightVertices[triangle[1]],
                lightVertices[triangle[2]]
            ))

        # Store depth values in shadow map
        for f in shadowFragments:
            if 0 <= f.y < self.height and 0 <= f.x < self.width:
                if self.shadowMap[f.y][f.x] > f.depth:
                    self.shadowMap[f.y][f.x] = f.depth

    def Rasterizer(self, v0, v1, v2):
        fragments = []

        # culling back face
        area = edgeSide(v0, v1, v2)
        if area < 0:
            return fragments

        # AABBox computation
        # compute vertex coordinates in screen space
        v0_image = np.array([0, 0])
        v0_image[0] = (v0[0]+1.0)/2.0 * self.width
        v0_image[1] = ((v0[1]+1.0)/2.0) * self.height

        v1_image = np.array([0, 0])
        v1_image[0] = (v1[0]+1.0)/2.0 * self.width
        v1_image[1] = ((v1[1]+1.0)/2.0) * self.height

        v2_image = np.array([0, 0])
        v2_image[0] = (v2[0]+1.0)/2.0 * self.width
        v2_image[1] = (v2[1]+1.0)/2.0 * self.height

        # compute the two point forming the AABBox
        A = np.min(np.array([v0_image, v1_image, v2_image]), axis=0)
        B = np.max(np.array([v0_image, v1_image, v2_image]), axis=0)

        # cliping the bounding box with the borders of the image
        max_image = np.array([self.width-1, self.height-1])
        min_image = np.array([0.0, 0.0])

        A = np.max(np.array([A, min_image]), axis=0)
        B = np.min(np.array([B, max_image]), axis=0)

        # cast bounding box to int
        A = A.astype(int)
        B = B.astype(int)
        # Compensate rounding of int cast
        B = B + 1

        # for each pixel in the bounding box
        for j in range(A[1], B[1]):
            for i in range(A[0], B[0]):
                x = (i+0.5)/self.width * 2.0 - 1
                y = (j+0.5)/self.height * 2.0 - 1

                p = np.array([x, y])

                area0 = edgeSide(p, v0, v1)
                area1 = edgeSide(p, v1, v2)
                area2 = edgeSide(p, v2, v0)

                # test if p is inside the triangle
                if (area0 >= 0 and area1 >= 0 and area2 >= 0):

                    # Computing 2d barricentric coordinates
                    lambda0 = area1/area
                    lambda1 = area2/area
                    lambda2 = area0/area

                    # one_over_z = lambda0 * 1/v0[2] + lambda1 * 1/v1[2] + lambda2 * 1/v2[2]
                    # z = 1/one_over_z

                    z = lambda0 * v0[2] + lambda1 * v1[2] + lambda2 * v2[2]

                    p = np.array([x, y, z])

                    l = v0.shape[0]
                    # interpolating
                    interpolated_data = v0[3:l] * lambda0 + \
                        v1[3:l] * lambda1 + v2[3:l] * lambda2

                    # Emiting Fragment
                    fragments.append(Fragment(i, j, z, interpolated_data))

        return fragments

    def fragmentShader(self, fragment, data):
        N = fragment.interpolated_data[0:3]
        N = N / np.linalg.norm(N)
        V = fragment.interpolated_data[3:6]
        V = V / np.linalg.norm(V)
        L = fragment.interpolated_data[6:9]
        L = L / np.linalg.norm(L)

        R = 2 * np.dot(L, N) * N - L

        ambient = 1.0
        diffuse = max(np.dot(N, L), 0)
        specular = np.power(max(np.dot(R, V), 0.0), 64)

        ka = 0.1
        kd = 0.9
        ks = 0.3
        phong = ka * ambient + kd * diffuse + ks * specular

        # Shadow calculation - corrected indices
        # Light space position (shifted by 3)
        lightPos = fragment.interpolated_data[11:15]
        if lightPos[3] != 0:  # Avoid division by zero
            # Perspective divide
            lightPosNDC = lightPos / lightPos[3]

            # Convert to shadow map coordinates
            shadowX = int((lightPosNDC[0] + 1.0) / 2.0 * self.width)
            shadowY = int((lightPosNDC[1] + 1.0) / 2.0 * self.height)

            # Check if fragment is within shadow map bounds
            if 0 <= shadowX < self.width and 0 <= shadowY < self.height:
                shadowDepth = self.shadowMap[shadowY][shadowX]
                currentDepth = lightPosNDC[2]

                # Apply bias to avoid shadow acne
                bias = 0.005

                # Check if fragment is in shadow
                if currentDepth > shadowDepth + bias:
                    # Fragment is in shadow, reduce lighting
                    phong = ka * ambient  # Keep only ambient lighting

        texture = 1  # Placeholder for texture
        color = np.array([phong, phong, phong]) * texture

        fragment.output = color

    def draw(self, vertices, triangles, data, lightData=None):
        # If lightData is provided, generate shadow map
        if lightData is not None:
            self.generateShadowMap(vertices, triangles, lightData)

        # Vertex processing
        # Update size to match vertex output
        self.newVertices = np.zeros((vertices.shape[0], 18))

        for i in range(vertices.shape[0]):
            # Pass lightData if available, otherwise use data as a fallback
            self.newVertices[i] = self.VertexShader(
                vertices[i],
                data,
                lightData if lightData is not None else data
            )

        # Rasterization
        fragments = []
        for i in triangles:
            fragments.extend(self.Rasterizer(
                self.newVertices[i[0]],
                self.newVertices[i[1]],
                self.newVertices[i[2]]
            ))

        # Fragment processing
        for f in fragments:
            self.fragmentShader(f, data)
            if self.depthBuffer[f.y][f.x] > f.depth:
                self.depthBuffer[f.y][f.x] = f.depth
                self.image[f.y][f.x] = f.output
