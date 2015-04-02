int return1 () {
	int a;
	a = 10;
	return a;
	a = 100;
	return a;
}

int return2 () {
	int a;
	a = 10;
	if (a == 10)
		return a;
	a = 100;
	return a;
}

int return3 () {
	int a;
	a = 10;
	if (a == 10) {
		return a;
	}
	a = 100;
	return a;
}

int return4 () {
	int i, j;
	i = 10;
	j = 1;
	while (i > 0) {
		while (j <= i) {
			if (i == j)
				return i;
			j = j + 1;
		}
		i = i - 1;
	}
	return 100;
}

int main() {
	int r1, r2, r3, r4;
	r1 = return1();
	r2 = return2();
	r3 = return3();
	r4 = return4();
}