public class HeftyInteger {

    private final byte[] ONE = {(byte) 1};
    private final byte[] ZERO = {(byte) 0};
    private byte[] val;

    /**
     * Construct the HeftyInteger from a given byte array
     *
     * @param b the byte array that this HeftyInteger should represent
     */
    public HeftyInteger(byte[] b) {
        val = b;
    }

    /**
     * Return this HeftyInteger's val
     *
     * @return val
     */
    public byte[] getVal() {
        return val;
    }

    /**
     * Return the number of bytes in val
     *
     * @return length of the val byte array
     */
    public int length() {
        return val.length;
    }

    /**
     * Add a new byte as the most significant in this
     *
     * @param extension the byte to place as most significant
     */
    public void extend(byte extension) {
        byte[] newv = new byte[val.length + 1];
        newv[0] = extension;
        for (int i = 0; i < val.length; i++) {
            newv[i + 1] = val[i];
        }
        val = newv;
    }

    /**
     * If this is negative, most significant bit will be 1 meaning most
     * significant byte will be a negative signed number
     *
     * @return true if this is negative, false if positive
     */
    public boolean isNegative() {
        return (val[0] < 0);
    }

    /**
     * Computes the sum of this and other
     *
     * @param other the other HeftyInteger to sum with this
     */
    public HeftyInteger add(HeftyInteger other) {
        byte[] a, b;
        // If operands are of different sizes, put larger first ...
        if (val.length < other.length()) {
            a = other.getVal();
            b = val;
        } else {
            a = val;
            b = other.getVal();
        }

        // ... and normalize size for convenience
        if (b.length < a.length) {
            int diff = a.length - b.length;

            byte pad = (byte) 0;
            if (b[0] < 0) {
                pad = (byte) 0xFF;
            }

            byte[] newb = new byte[a.length];
            for (int i = 0; i < diff; i++) {
                newb[i] = pad;
            }

            for (int i = 0; i < b.length; i++) {
                newb[i + diff] = b[i];
            }

            b = newb;
        }

        // Actually compute the add
        int carry = 0;
        byte[] res = new byte[a.length];
        for (int i = a.length - 1; i >= 0; i--) {
            // Be sure to bitmask so that cast of negative bytes does not
            //  introduce spurious 1 bits into result of cast
            carry = ((int) a[i] & 0xFF) + ((int) b[i] & 0xFF) + carry;

            // Assign to next byte
            res[i] = (byte) (carry & 0xFF);

            // Carry remainder over to next byte (always want to shift in 0s)
            carry = carry >>> 8;
        }

        HeftyInteger res_li = new HeftyInteger(res);

        // If both operands are positive, magnitude could increase as a result
        //  of addition
        if (!this.isNegative() && !other.isNegative()) {
            // If we have either a leftover carry value or we used the last
            //  bit in the most significant byte, we need to extend the result
            if (res_li.isNegative()) {
                res_li.extend((byte) carry);
            }
        }
        // Magnitude could also increase if both operands are negative
        else if (this.isNegative() && other.isNegative()) {
            if (!res_li.isNegative()) {
                res_li.extend((byte) 0xFF);
            }
        }

        // Note that result will always be the same size as biggest input
        //  (e.g., -127 + 128 will use 2 bytes to store the result value 1)
        return res_li;
    }

    /**
     * Negate val using two's complement representation
     *
     * @return negation of this
     */
    public HeftyInteger negate() {
        byte[] neg = new byte[val.length];
        int offset = 0;

        // Check to ensure we can represent negation in same length
        //  (e.g., -128 can be represented in 8 bits using two's
        //  complement, +128 requires 9)
        if (val[0] == (byte) 0x80) { // 0x80 is 10000000
            boolean needs_ex = true;
            for (int i = 1; i < val.length; i++) {
                if (val[i] != (byte) 0) {
                    needs_ex = false;
                    break;
                }
            }
            // if first byte is 0x80 and all others are 0, must extend
            if (needs_ex) {
                neg = new byte[val.length + 1];
                neg[0] = (byte) 0;
                offset = 1;
            }
        }

        // flip all bits
        for (int i = 0; i < val.length; i++) {
            neg[i + offset] = (byte) ~val[i];
        }

        HeftyInteger neg_li = new HeftyInteger(neg);

        // add 1 to complete two's complement negation
        return neg_li.add(new HeftyInteger(ONE));
    }

    /**
     * Implement subtraction as simply negation and addition
     *
     * @param other HeftyInteger to subtract from this
     * @return difference of this and other
     */
    public HeftyInteger subtract(HeftyInteger other) {
        return this.add(other.negate());
    }


    /**
     * Compute the product of this and other
     *
     * @param other HeftyInteger to multiply by this
     * @return product of this and other
     */
    public HeftyInteger multiply(HeftyInteger other) {
        // YOUR CODE HERE (replace the return, too...)
        HeftyInteger a, b;
        a = this;  //Multiplier
        b = other; //Multiplicand

        if (this.isNegative()) {    //Check if this or other is negative, turn positive to make it easier to multiply
            a = a.negate();
        }
        if (other.isNegative()) {
            b = b.negate();
        }

        HeftyInteger result = new HeftyInteger(new byte[a.length() + b.length()]);

        // Actually compute the multiply
        for (int i = a.length() - 1; i >= 0; i--) {
            int bit = 0x01; //0x01 = 00000001

            for (int j = 0; j < 8; j++) {           //Go through each bit, adding partial product whenever bit in multiplier = 1
                if ((a.getVal()[i] & bit) != 0) {   //Checks if LSB is 1
                    result = result.add(b);     //Increment result when 1 in the multiplier
                }

                b = b.appendLeftShift(); //Left shift multiplicand
                bit <<= 1;   //Left shift
            }
        }

        if (this.isNegative() == other.isNegative()) {  //If this and other were same sign, return positive else return negative
            return result;
        } else {
            return result.negate();
        }
    }

    /**
     * Left Shift
     */
    public HeftyInteger appendLeftShift() {
        byte[] appended = new byte[val.length + 1];
        int oldSign = 0, sign;

        for (int i = 1; i <= val.length; i++) {
            sign = (val[val.length - i] & 0x80) >> 7;   //Isolate and get the leading sign bit
            appended[appended.length - i] = (byte) (val[val.length - i] << 1);  //Add 0 to the end
            appended[appended.length - i] |= oldSign;   //Preserve sign
            oldSign = sign;
        }

        return new HeftyInteger(appended);
    }

    /**
     * Run the extended Euclidean algorithm on this and other
     *
     * @param other another HeftyInteger
     * @return an array structured as follows:
     * 0:  the GCD of this and other
     * 1:  a valid x value
     * 2:  a valid y value
     * such that this * x + other * y == GCD in index 0
     */
    public HeftyInteger[] XGCD(HeftyInteger other) {
        // YOUR CODE HERE (replace the return, too...)
        HeftyInteger[] result = new HeftyInteger[3], divide;
        HeftyInteger a = this, b = other, divideResult, sPrev = new HeftyInteger(ONE), tPrev = new HeftyInteger(ZERO), sCurr = new HeftyInteger(ZERO), tCurr = new HeftyInteger(ONE), sUpdate, tUpdate;

        if (other.val[other.length() - 1] == 0x00) {    //If one of the values are 0, know GCD along with s and t
            result[0] = a;
            result[1] = new HeftyInteger(ONE);
            result[2] = other;
            return result;
        }

        while (!b.subtract(new HeftyInteger(ONE)).isNegative()) {      //Calculate the XGCD, break out of loop when b = 0
            divide = a.divide(b);
            divideResult = divide[0];
            a = b;
            b = divide[1];

            sUpdate = sPrev.subtract(divideResult.multiply(sCurr));    //Keep track of linear coefficients as we calculate the divide and mod
            tUpdate = tPrev.subtract(divideResult.multiply(tCurr));
            sPrev = sCurr;
            tPrev = tCurr;
            sCurr = sUpdate;
            tCurr = tUpdate;
        }

        result[0] = a;
        result[1] = sPrev;
        result[2] = tPrev;
        return result;
    }

    public HeftyInteger[] divide(HeftyInteger other) {    //Find quotient = this/other, returns both quotient in index 0 and remainder in index 1
        HeftyInteger[] quotientAndRemainder = new HeftyInteger[2];
        HeftyInteger a, b, remainder, quotient;
        a = this;  //dividend
        b = other; //divisor
        quotient = new HeftyInteger(ZERO);  //Set quotient = 0
        remainder = a;  //Set remainder = dividend

        int bitsShifted = 0;
        while (!a.subtract(b).isNegative()) {   //Shift the divisor corresponding to the dividend
            b = b.appendLeftShift();
            bitsShifted++;
        }

        for (int i = 0; i < bitsShifted; i++) { //For the number of bits we previously shifted in dividend
            b = b.appendRightShift();
            quotient = quotient.appendLeftShift();

            if (!remainder.subtract(b).isNegative()) {  //if(divisor < remainder)
                remainder = remainder.subtract(b);
                quotient = quotient.add(new HeftyInteger(ONE));
            }
        }

        quotientAndRemainder[0] = quotient;
        quotientAndRemainder[1] = remainder;
        return quotientAndRemainder;
    }

    /**
     * Right shift
     */
    public HeftyInteger appendRightShift() {
        byte[] appended = new byte[val.length];
        int oldSign = 0, sign;

        for (int i = 0; i < appended.length; i++) {
            sign = val[i] & 0x01;
            appended[i] = (byte) ((val[i] >> 1) & 0x7f);
            appended[i] |= oldSign << 7;
            oldSign = sign;
        }

        return (new HeftyInteger(appended));
    }


}

