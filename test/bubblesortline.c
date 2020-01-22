#include <stdio.h>
void swap(int *a, int *b) {int tmp = *a;*a = *b;*b = tmp;}void bubblesort(int list[],int size) {int i, j;for(i = 0; i < size - 1; i++) {for(j = 0; j < size-i-1; j++) {if(list[j] > list[j+1]) {swap(&list[j], &list[j+1]);}}}}void arrprint(int arr[], int size) {for(int i = 0; i < size; i++) {printf("%d ", arr[i]);}printf("\n");}int main(void) {int list[10] = {0, 3, 5, 7, 9, 4, 2, 1, 6, 8};int size = 10;bubblesort(list, size);arrprint(list, size);return 0;}

        
