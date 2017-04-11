package com.van.hid;

/**
 * Created by yuhc on 16-2-21.
 */
public class TKeyPair {
    private byte[] priKey;
    private byte[] pubKey;
    
    private int type;
    
    public TKeyPair(byte[] priKey, byte[] pubKey){
        this.priKey = priKey;
        this.pubKey = pubKey;
    }

    public byte[] getPriKey() {
        return priKey;
    }

    public void setPriKey(byte[] priKey) {
        this.priKey = priKey;
    }

    public byte[] getPubKey() {
        return pubKey;
    }

    public void setPubKey(byte[] pubKey) {
        this.pubKey = pubKey;
    }

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
