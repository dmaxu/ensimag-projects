#include <thread>
#include <iostream>
#include <mutex>
#include <condition_variable>
#include <cassert>
#include <vector>

using namespace std;
int compteur = 0;
mutex m;
condition_variable c;


void hello_world(){
    unique_lock<mutex>lg(m);

    cout <<"Hello World !\n";
    compteur++;
    c.notify_one();
}

void done(){
    unique_lock<mutex>lg(m);
    
    c.wait(lg,[](){return compteur>=10;});
    cout <<"Done !\n";
}

int main(){
    vector<thread> ThreadVector;
    for (int i=0;i<10;i++){
        ThreadVector.emplace_back([&](){hello_world();});
    }
    ThreadVector.emplace_back([&](){done();});

    for (auto & t:ThreadVector){
        t.join();
    }
}