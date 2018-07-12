/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2014-2016 Inria
 * Copyright (C) 2011-2013 Bordeaux University
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, version 2.1.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; If not, see
 * <http://www.gnu.org/licenses/>.
 */
package fr.inria.papart.multitouch;

/**
 *
 * @author s. conversy from n. roussel c++ version
 */


public class OneEuroFilter {

    double freq;
    double mincutoff;
    double beta_;
    double dcutoff;
    LowPassFilter x;
    LowPassFilter dx;
    double lasttime;
    static double UndefinedTime = -1;

    double alpha(double cutoff) {
        double te = 1.0 / freq;
        double tau = 1.0 / (2 * Math.PI * cutoff);
        return 1.0 / (1.0 + tau / te);
    }

    public void setFrequency(double f) throws Exception {
        if (f <= 0) {
            throw new Exception("freq should be >0");
        }
        freq = f;
    }

    public void setMinCutoff(double mc) throws Exception {
        if (mc <= 0) {
            throw new Exception("mincutoff should be >0");
        }
        mincutoff = mc;
    }

    public void setBeta(double b) {
        beta_ = b;
    }

    public void setDerivateCutoff(double dc) throws Exception {
        if (dc <= 0) {
            throw new Exception("dcutoff should be >0");
        }
        dcutoff = dc;
    }

    public OneEuroFilter(double freq) throws Exception {
        init(freq, 1.0, 0.0, 1.0);
    }

    public OneEuroFilter(double freq, double mincutoff) throws Exception {
        init(freq, mincutoff, 0.0, 1.0);
    }

    public OneEuroFilter(double freq, double mincutoff, double beta_) throws Exception {
        init(freq, mincutoff, beta_, 1.0);
    }

    public OneEuroFilter(double freq, double mincutoff, double beta_, double dcutoff) throws Exception {
        init(freq, mincutoff, beta_, dcutoff);
    }

    private void init(double freq,
            double mincutoff, double beta_, double dcutoff) throws Exception {
        setFrequency(freq);
        setMinCutoff(mincutoff);
        setBeta(beta_);
        setDerivateCutoff(dcutoff);
        x = new LowPassFilter(alpha(mincutoff));
        dx = new LowPassFilter(alpha(dcutoff));
        lasttime = UndefinedTime;
    }

    public double filter() throws Exception {
        return filter(lastValue(), UndefinedTime);
    }

    public double filter(double value) throws Exception {
        return filter(value, UndefinedTime);
    }

    private double lastValue; 
    public double lastValue(){
        return lastValue;
    }
    
    public double filter(double value, double timestamp) throws Exception {
        // update the sampling frequency based on timestamps
        if (lasttime != UndefinedTime && timestamp != UndefinedTime) {
            freq = 1.0 / (timestamp - lasttime);
        }
        
        lasttime = timestamp;
        // estimate the current variation per second
        double dvalue = x.hasLastRawValue() ? (value - x.lastRawValue()) * freq : 0.0; // FIXME: 0.0 or value?
        double edvalue = dx.filterWithAlpha(dvalue, alpha(dcutoff));
        // use it to update the cutoff frequency
        double cutoff = mincutoff + beta_ * Math.abs(edvalue);
        // filter the given value
        lastValue = x.filterWithAlpha(value, alpha(cutoff));
        return lastValue;
    }

//    public static void main(String[] args) throws Exception {
//        //randSeed();
//        double duration = 10.0; // seconds
//        double frequency = 120; // Hz
//        double mincutoff = 1.0; // FIXME
//        double beta = 1.0;      // FIXME
//        double dcutoff = 1.0;   // this one should be ok
//
//        System.out.print(
//                "#SRC OneEuroFilter.java" + "\n"
//                + "#CFG {'beta': " + beta + ", 'freq': " + frequency + ", 'dcutoff': " + dcutoff + ", 'mincutoff': " + mincutoff + "}" + "\n"
//                + "#LOG timestamp, signal, noisy, filtered" + "\n");
//
//        OneEuroFilter f = new OneEuroFilter(frequency,
//                mincutoff,
//                beta,
//                dcutoff);
//        for (double timestamp = 0.0; timestamp < duration; timestamp += 1.0 / frequency) {
//            double signal = Math.sin(timestamp);
//            double noisy = signal + (Math.random() - 0.5) / 5.0;
//            double filtered = f.filter(noisy, timestamp);
//            System.out.println("" + timestamp + ", "
//                    + signal + ", "
//                    + noisy + ", "
//                    + filtered);
//        }
//    }
}