int main(void) {
        int i = 0, j, k;
        j = 0;

        for(i = 0; i < 25; i++) {
                j += i;
        }

        switch(j)
        {
                case 1:
                k = 1;
                break;
                case 2:
                k = 2;
                break;
                case 3:
                k=3;
                break;
                default:
                k = -5;
                break;
        }

        j = j - 5;

        j *= 2;

        j = j/3;

        return k;
}
