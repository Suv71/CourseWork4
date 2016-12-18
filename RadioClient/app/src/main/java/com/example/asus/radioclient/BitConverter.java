package com.example.asus.radioclient;

/**
 * Created by ASUS on 01.12.2016.
 */

public final class BitConverter
{
    public static byte[] getBytes(int v)
    {
        byte[] writeBuffer = new byte[4];
        writeBuffer[3] = (byte) ((v >>> 24) & 0xFF);
        writeBuffer[2] = (byte) ((v >>> 16) & 0xFF);
        writeBuffer[1] = (byte) ((v >>> 8) & 0xFF);
        writeBuffer[0] = (byte) ((v >>> 0) & 0xFF);
        return writeBuffer;
    }

    public static int toInt32(byte[] data, int offset)
    {
        return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8)
                | ((data[offset + 2] & 0xFF) << 16)
                | ((data[offset + 3] & 0xFF) << 24);
    }

    public static int[] ArraysByteToInt(byte[] in)
    {
        if(in != null)
        {
            int[] res = new int[in.length / 4];

            int j = 0;
            for (int i = 0; i < res.length; i++)
            {
                res[i] = toInt32(in, j);
                j += 4;
            }

            return res;
        }
        else
        {
            return null;
        }
    }

    public static byte[] MergeArrays(byte[] first, byte[] second)
    {
        byte[] res;
        if(first != null && second != null)
        {
            res = new byte[first.length + second.length];
            for(int i = 0; i < first.length; i++)
            {
                res[i] = first[i];
            }

            int j = first.length;
            for(int i = 0; i < second.length; i++)
            {
                res[j++] = second[i];
            }

            return res;
        }
        else
        {
            return null;
        }

    }
}
