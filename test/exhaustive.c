int main (void)
{
        long unsigned i, j;
        double k;
        float l;
        short m;
        char n[] = "This is a string";
        const char c = 'c';
        static int s = 2;

        while(1) {
                if(1)
                        break;
        }

        do{
                if(0) {
                        k = -5;
                }
                else {
                        k = 1;
                }
        }while(0);

        for(i = 0; i<5; i++) {
                i+=1;
        }
        i-=1;
        i/=2;
        i*=0;

        i%=2;
        i++;
        i--;

        i^=0xFF;
        i>>=32;
        i<<=1;
        i&=0;
        i|=1;

        i+1;
        i-1;
        i * 5;
        i/2;
        i % 2;

        return 0;
}

/*
variableModifier auto volatile extern register
controlSpecifier continue goto
booleanOperator && == || <= >= < > !=
structStmt struct . ->
switchStmt switch case default
enumSpecifier enum
unionSpecifier union
typedefSpecifier typedef
bitOperator ^ & | ! << >>
preprocessorSpecifier #
*/
