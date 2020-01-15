// @author Brandon Fleming
// @date January 15th, 2020
// @file fizzbuzz.c
//
// @detail Implements a fizzbuzz

#include <stdio.h>

int main(void)                                                                 {
  int i, target                                                                ;
  target = 50                                                                  ;
  for(i=0; i <= target; i++)                                                   {
    printf("%d: ", i)                                                          ;
    if(i%3 == 0)                                                               {
      printf("Fizz")                                                          ;}
    if(i%5 == 0)                                                               {
      printf("Buzz")                                                          ;}
  printf("\n")                                                                ;}
  return 0                                                                     ;
                                                                               }
