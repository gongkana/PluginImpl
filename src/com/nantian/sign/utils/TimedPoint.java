package com.nantian.sign.utils;

public class TimedPoint {
    public float x;
    public float y;
    public float presure;
    public long timestamp;

    public TimedPoint set(float x, float y,float presure,long time) {
        this.x = x;
        this.y = y;
        this.timestamp = time;
        this.presure = presure;
        return this;
    }
    public TimedPoint (float x, float y,float presure,long time) {
        this.x = x;
        this.y = y;
        this.timestamp = time;
        this.presure = presure;
    }
    public TimedPoint(){

    }
    public float velocityFrom(TimedPoint start) {
        float velocity = distanceTo(start) / (this.timestamp - start.timestamp);
        if (velocity != velocity) return 0f;
        return velocity;
    }

    public float distanceTo(TimedPoint point) {
        return (float) Math.sqrt(Math.pow(point.x - this.x, 2) + Math.pow(point.y - this.y, 2));
    }
}
