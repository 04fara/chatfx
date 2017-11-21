package algorithms;

public class Repetition implements Encoding {
    private int count;

    public Repetition(int count) {
        this.count = count;
    }

    @Override
    public byte[] encode(byte[] input) {
        //creating resulting array
        byte[] result = new byte[input.length * count];
        //loop for each byte of input
        for (int i = 0; i < input.length; i++)
            //loop for each bit of encoded byte
            for (int j = 0; j < 8 * count; j++)
                //if current bit==1 set it to the result in corresponding position
                if ((input[i] & 1 << (7 - j / count)) != 0)
                    result[i * count + j / 8] |= 1 << (7 - j % 8);
        return result;
    }

    @Override
    public byte[] decode(byte[] input) {
        //creating resulting array
        byte[] result = new byte[input.length / count];
        //loop for each byte of input
        for (int i = 0; i < result.length; i++)
            //loop for each bit of decoded byte
            for (int j = 0; j < 8; j++) {
                //number of 1 in 1-bit block
                byte temp = 0;
                //loop for 1 1-bit block
                for (int k = 0; k < count; k++)
                    if ((input[i * count + (j * count + k) / 8] & (1 << (7 - (j * count + k) % 8))) > 0)
                        temp++;
                //set result's current bit to 1 when (sum of bits in 1-bit encoded block)>repetition/2
                if (temp > count / 2)
                    result[i] += 1 << (7 - j);
            }
        return result;
    }
}
