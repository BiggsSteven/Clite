int main ( ) {
    n = 3;
    i = 0;
    f = 1.0;
    while (i < n) {
        i = i + 1.0;		// implicit cast
        f = f * float(i);	// explicit cast
    } 
}
