#ifndef _TEST16_MSG_H_
#define _TEST16_MSG_H_

struct tst16 {
    int count;
    int fid;
};

int proc16_1(void *arg);
int proc16_2(void *arg);
int proc16_3(void *arg);
#endif /* _TEST16_MSG_H_ */
