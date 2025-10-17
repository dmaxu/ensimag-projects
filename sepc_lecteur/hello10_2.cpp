#include <thread>
#include <iostream>
#include <mutex>
#include <condition_variable>
#include <cassert>
#include <vector>

using namespace std;

class Data
{
public:
    int cnt = 0;
    mutex m;
    condition_variable c;
};

void hello_world(Data &data)
{
    unique_lock<mutex> lg(data.m);
    cout << "Hello World !\n";
    data.cnt++;
    data.c.notify_one();
}

void done(Data &data)
{
    unique_lock<mutex> lg(data.m);

    data.c.wait(lg, [&data]()
                { return ((data.cnt) >= 10); });
    cout << "Done !\n";
}

int main()
{
    vector<thread> ThreadVector;
    Data data = Data();
    for (int i = 0; i < 10; i++)
    {
        ThreadVector.emplace_back([&]()
                                  { hello_world(data); });
    }
    ThreadVector.emplace_back([&]()
                              { done(data); });

    for (auto &t : ThreadVector)
    {
        t.join();
    }
    delete &data;
}