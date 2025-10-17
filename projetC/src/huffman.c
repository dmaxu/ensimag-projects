#include <stdint.h>
#include <stdlib.h>
#include <stdbool.h>
#include <string.h>
#include <stdio.h>
#include "htables.h"

// Struct for Huffman tree node
typedef struct TreeNode {
  struct TreeNode *left;
  struct TreeNode *right;
  bool is_leaf;
  uint16_t symbol;
} TreeNode;

TreeNode* create_node(uint16_t symbol) {
    //create empty tree
    TreeNode* node = (TreeNode*)malloc(sizeof(TreeNode));
    if (node != NULL) {
        node->left = NULL;
        node->right = NULL;
        node->symbol = symbol;
        node->is_leaf = false;
    }
    return node;
}




bool insert_node(TreeNode *current, uint16_t symbol, int  len) {
  if ((current == NULL) || (current->is_leaf) || (len < 0)) {
    return false;
  }

  if ((current->left == NULL) && (len == 0)) {
    TreeNode *node = create_node(symbol);
    node->is_leaf = true;
    current->left = node;
    return true;
  }
  if ((current->right == NULL) && (len == 0)) {
    TreeNode *node = create_node(symbol);
    node->is_leaf = true;
    current->right = node;
    return true;
  }
  if (current->left == NULL) {
    TreeNode *node = create_node(INT16_MAX);
    node->is_leaf = false;
    current->left = node;
  }
  if (current->right == NULL) {
    TreeNode *node = create_node(INT16_MAX);
    node->is_leaf = false;
    current->right = node;
  }

  if (insert_node(current->left, symbol, len - 1))
    return true;
    
  else if (insert_node(current->right, symbol, len - 1))
    return true;
  else return false;
}

TreeNode *build_huffman_tree(uint8_t sample_type, uint8_t color_component) {
  uint8_t *symbols_table = htables_symbols[sample_type][color_component];
  uint8_t(*nb_symb_per_length) =
      htables_nb_symb_per_lengths[sample_type][color_component];
  size_t treated_symbols = 0;
  TreeNode *root = create_node(INT16_MAX);
  for (int length = 0; length < 16; length++) {
    for (int symbol = 0; symbol < nb_symb_per_length[length]; symbol++) {
      insert_node(root, symbols_table[treated_symbols + symbol], length);
    }
    treated_symbols = treated_symbols + nb_symb_per_length[length];
  }
  return root;
}

void free_tree(TreeNode *root) {
  if (root != NULL) {
    free_tree(root->left);
    free_tree(root->right);
    free(root);
  }
}




void huffman_to_tab(TreeNode *current, char **huffman_codes, char *path) {
  if (current == NULL) {
    return;
  }
  if (current->is_leaf) {
    strcpy(huffman_codes[current->symbol],path);
    path[0] = '\0';
    return;
  }
  if (current->left != NULL) {
    char path1[16];
    strcpy(path1,path);
    strcat(path1, "0");
    huffman_to_tab(current->left, huffman_codes, path1);
  }
  if (current->right != NULL) {
    char path1[16];
    strcpy(path1,path);
    strcat(path1, "1");
    huffman_to_tab(current->right, huffman_codes, path1);
  }
}


// int main() {
//   // Example usage
//   char *huffman_codes[256];
//   for (int i = 0; i < 256; i++) {
//     huffman_codes[i] = malloc(16 * sizeof(char*));
//     huffman_codes[i][0] = '\0';
//   }
//   uint8_t sample_type = 1;
//   uint8_t color_component = 1;

//   TreeNode *tree = build_huffman_tree(sample_type, color_component);
//   char *path = malloc(17 * sizeof(char));
//   for (int i = 0; i < 17; i++) {
//     path[i] = '\0';
//   }
//   huffman_to_tab(tree, huffman_codes, path);
//   for (int i = 0; i < 256; i++) {
//     if (strlen(huffman_codes[i])>0){
//         printf(",\"%s\"", huffman_codes[i]);
//     }
//     else {
//         printf(",\"\"");
//     }
//     }
//     free_tree(tree);
//     free(path);
//     for (int i = 0; i < 256; i++) {
//         free(huffman_codes[i]);
//     }
// }