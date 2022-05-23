package fr.inria.papart.apps;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import redis.clients.jedis.Jedis;


// TODO Load deps from classpath file
// https://stackoverflow.com/questions/1464291/how-to-really-read-text-file-from-classpath-in-java

/**
 *
 * @author Jeremy Laviole, <laviole@rea.lity.tech>
 */
@SuppressWarnings("serial")
public abstract class NectarApplication {

    public static final String REDIS_PORT = "6379";
    public static final String REDIS_HOST = "localhost";

    static protected String host = REDIS_HOST;
    static protected String port = REDIS_PORT;

    static protected boolean isUnique = false;
    static protected boolean isVerbose = false;
    static protected boolean isSilent = false;

    static protected Options options = new Options();

    protected static void addDefaultOptions(Options options) {
        options.addOption("h", "help", false, "print this help.");
        options.addOption("v", "verbose", false, "Verbose activated.");
        options.addOption("s", "silent", false, "Silent activated.");
        options.addOption("u", "unique", false, "Unique mode, run only once.");
        options.addOption("rp", "redisport", true, "Redis port, default is: " + REDIS_PORT);
        options.addOption("rh", "redishost", true, "Redis host, default is: " + REDIS_HOST);
    }

    protected static void parseDefaultOptions(CommandLine cmd) {

        if (cmd.hasOption("h")) {
            die("", true);
        }
        if (cmd.hasOption("u")) {
            isUnique = true;
        }
        if (cmd.hasOption("v")) {
            isVerbose = true;
        }
        if (cmd.hasOption("s")) {
            isSilent = true;
        }
        if (cmd.hasOption("rh")) {
            host = cmd.getOptionValue("rh");
        }
        if (cmd.hasOption("rp")) {
            port = cmd.getOptionValue("rp");
        }
    }

    protected static Jedis connectRedis() {
        try {
            Jedis redis = new Jedis(host, Integer.parseInt(port));
            if (redis == null) {
                throw new Exception("Cannot connect to server. ");
            }
            return redis;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return null;
        }
        // redis.auth("156;2Asatu:AUI?S2T51235AUEAIU");
    }

    public static void log(String normal, String verbose) {

        if (isSilent) {
            return;
        }
        if (normal != null && !"".equals(normal)) {
            System.out.println(normal);
        }
        if (isVerbose) {
            System.out.println(verbose);
        }
    }

    public static void die(String why) {
        die(why, false);
    }

    public static void die(String why, boolean usage) {
        if (usage) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Natar application", options);
        }
        System.out.println(why);
        System.exit(-1);
    }

}
