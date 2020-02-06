int main()
{
        int i = 0;

        i^=0xFF;
        i>>=32;
        i<<=1;
        i&=0;
        i|=1;

        return 0;
}
