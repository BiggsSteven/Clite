int factorial (int n) {
// computes the factorial of n, given n >= 0
	if (n < 2)
		return 1;
	else
		return n * factorial(n-1);
}

int main() {
	int f;
	f = factorial(3);
}