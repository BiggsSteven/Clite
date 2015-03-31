int main()
{
	char num[2], temp;
	num[0] = '2';
	num[1] = '1';

	if (num[0] > num[1])
	{
		temp = num[1];
		num[1] = num[0];
		num[0] = temp;
	}
}