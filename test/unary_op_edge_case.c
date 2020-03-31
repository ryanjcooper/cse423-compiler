int main() {
	int i = 5;
	int j = 6;
	
	i = i++ + 2;
	i = 3 + ++i;
	j = j++ - --i;
	j = --j + i-- - --i;
	
	return i;
}