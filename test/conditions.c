int main()
{
	int a = 7;
	if (a < 4) {
		a += 3;
	} else if (a == 7) {
		a += 2;
	} else if (a > 7) {
		a *= 10;
	} else {
		a *= 5;
	}

	a++;
	a *= 5;
	return a;
}
