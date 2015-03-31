int main ( ) {
    int orig[5], count[5], sorted[5];
	int n, i, j;
	n = 5;
	orig[0] = 2;
	orig[1] = 9;
	orig[2] = 5;
	orig[3] = 0;
	orig[4] = 6;
	
	i = 0;
	while (i < n) {
	    count[i] = 0;
		i = i + 1;
	}
	i = 0;
	while (i < n - 1) {
	    j = i + 1;
		while (j < n) {
		    if (orig[i] < orig[j]) {
			    count[j] = count[j] + 1;
			}
			else {
			    count[i] = count[i] + 1;
			}
			j = j + 1;
		}
		i = i + 1;
	}
	i = 0;
	while (i < n) {
	    sorted[count[i]] = orig[i];
		i = i + 1;
	}
}