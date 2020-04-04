int main()
{
    int *i;
    int j;
    
    j = 0;
    
    i = &j;
    
    *i = *i + 1;
    
    return *i;
}