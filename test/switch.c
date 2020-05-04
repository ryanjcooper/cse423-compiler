int main() {
        int i = 0;
        int x;

        switch (i) {
		case 0:
			x = i;
			return x;
		case 1:
			return i + 1;
		default:
			x = i + 1;
			return x + 1;
        }

//        if (i == 0) {
//        	x = i;
//        	return x;
//        } else if (i == 1) {
//        	return i + 1;
//        } else {
//        	x = i + 1;
//        	return x + 1;
//        }

        return 0;
}
