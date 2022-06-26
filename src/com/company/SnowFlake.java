package com.company;

import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Enumeration;
import java.util.Random;

public class SnowFlake {
    private static final int UNUSED_BITS = 1; // Sign bit, Unused (always set to 0)
    private static final int TIMESTAMP_BITS = 41;
    private static final int WORKER_ID_BITS = 10;
    private static final int SEQUENCE_BITS = 12;

    private static final long maxWorkerId = (long) Math.pow(2,WORKER_ID_BITS)-1; //==  2^(10-1) == 1023
    private static final long maxSequence = (long) Math.pow(2,SEQUENCE_BITS)-1; //== 2^(12-1) == 4095

    /*Instant -> Dai dien cho mot thoi diem dong thoi gian ->
     * Epoch == Start cua Instant == {1970/01/01 (1970–01-01T00:00:00Z) }*/
    private static final long EPOCH_START = Instant.EPOCH.toEpochMilli();

    private final long workerId;
    private final long customEpoch;

    /*Save value Bien vao Main Memory -> Khi bien Volatile duoc update -> Cac Thread khac cung se nhan duoc thong bao */
    private volatile long lastTimestamp = -1;
    private volatile long sequence = 0;

    //Constructor()
    public SnowFlake(long workerId, long customEpoch) {
        if(workerId < 0 || workerId > maxWorkerId) {
            throw new IllegalArgumentException(String.format("NodeId must be between %ld and %ld", 0, maxWorkerId));
        }
        this.workerId = workerId;
        this.customEpoch = customEpoch;
    }

    public SnowFlake(long workerId) {
        this(workerId, EPOCH_START);
    }

    public SnowFlake() {
        this.workerId = createWorkerId();
        this.customEpoch = EPOCH_START;
    }

    public synchronized long newIdSequence() { //Lock method cho đến khi Thread xong task
        long currentTimestamp = newTimeStamp();

        if (currentTimestamp == lastTimestamp) {
            sequence++;
            if(sequence>maxSequence) { //sequence>4095 trong 1 milisec
                // Sequence Exhausted, wait till next millisecond.
                currentTimestamp = waitNextMillis(currentTimestamp);
                sequence=0;
            }
        } else {
            // reset sequence to start with zero for the next millisecond
            sequence = 0;
        }

        lastTimestamp = currentTimestamp;
        long id = currentTimestamp << (WORKER_ID_BITS + SEQUENCE_BITS)
                | (workerId << SEQUENCE_BITS)
                | sequence;
        return id;
    }

    public synchronized long newId() { //Lock method cho đến khi Thread xong task
        long currentTimestamp = newTimeStamp();
        Random randomSequence = new Random();
        if (currentTimestamp == lastTimestamp) {
            sequence = (long) Math.floor(Math.random()*(maxSequence+1));
        } else {
            // reset sequence to start with zero for the next millisecond
            sequence = 0;
        }

        lastTimestamp = currentTimestamp;
        long id = currentTimestamp << (WORKER_ID_BITS + SEQUENCE_BITS)
                | (workerId << SEQUENCE_BITS)
                | sequence;
        return id;
    }

    private long newTimeStamp() {
        return Instant.now().toEpochMilli() - customEpoch;
    }


    private long waitNextMillis(long currentTimestamp) {
        while (currentTimestamp == lastTimestamp) { // Block and wait till next millisecond
            currentTimestamp = newTimeStamp();
        }
        return currentTimestamp;
    }

    private long createWorkerId() { //Get Mac Address for all network interface on Computer
        long workerId;
        try {
            StringBuilder sb = new StringBuilder();
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                byte[] mac = networkInterface.getHardwareAddress(); //Tra ve kieu Byte
                if (mac != null) {
                    for(byte macPort: mac) {
                        sb.append(String.format("%02X", macPort)); // Nối chuỗi vào String Builder, String được Format dưới dạng Hex(16) (VD Ouput: 10 = OA)
                    }
                }
            }

            workerId = sb.toString().hashCode(); //Memory Address cua String Builder sb duoi dang Hex (HashCode bằng nhau -> Object bằng nhau)
        } catch (Exception ex) {
            workerId= (new SecureRandom().nextInt());  //Tao Random mot gia tri Worker ID không thể dự đoán trước để sử dụng nếu có lỗi
        }

        if(workerId>maxWorkerId) return 0;
        return workerId;
    }

    @Override
    public String toString() {
        return  workerId +"";
    }
}
