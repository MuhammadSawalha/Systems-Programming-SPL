package bguspl.set;

import java.util.logging.Logger;

public class Env {

    public static final String Config = null;
    public final Logger logger;
    public final Config config;
    public final UserInterface ui;
    public final Util util;

    public Env(Logger logger, Config config, UserInterface ui, Util util) {
        this.logger = logger;
        this.config = config;
        this.ui = ui;
        this.util = util;
    }
}
