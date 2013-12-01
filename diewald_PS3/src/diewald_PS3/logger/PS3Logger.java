package diewald_PS3.logger;

public final class PS3Logger {
  private static int thrown_logs = 0;

  private PS3Logger(){}

  public static void log( TYPE type, Logable class_instance, String... messages){
    if( type == null || !type.isActive() )
      return;
    
    StackTraceElement[] elements = new Throwable().getStackTrace();
    
    StackTraceElement ste = null;
    if( elements.length >= 1)
      ste = new StackTraceElement(elements[1].getClassName(), 
                                  elements[1].getMethodName(), 
                                  elements[1].getFileName(), 
                                  elements[1].getLineNumber() );

    String type_out    = "\n" + type.getPre();
    String location    = "\n\tlocation:   "+ (ste == null ? "UNKNOWN" : ste.toString());
    String index       = (class_instance != null  ?  "\n\ton device:  "+ class_instance.getIndex()  : "");
    String message_out = "";
    
    if( messages != null )
      for(String msg : messages )
        if ( msg != null && msg.length() != 0)
          message_out += "\n\tmessage:    "+msg;

    switch( type ){
      case ERROR:
      case WARNING:
        System.err.println( type_out+ location + index + message_out );
        break;
      case INFO:
        System.out.println( type_out + index + message_out);
        break;
      case DEBUG:
        System.out.println( type_out+ location + index + message_out );
        break;
    }
    thrown_logs++;
  }
  
  public static void log( TYPE type){
    log(type, null, type.get());
  }
  
  public enum TYPE{
    ERROR   ("#_PS3_ERROR___#"),
    WARNING ("#_PS3_WARNING_#"),
    INFO    ("#_PS3_INFO____#"),
    DEBUG   ("#_PS3_DEBUG___#"),
    ;
    private String msg_pre_;
    private String msg_ = "_";
    private boolean active_ = true;
    
    private TYPE(String msg_pre){
      this.msg_pre_ = msg_pre;
    }
    public TYPE set(String msg){
      msg_ = msg;
      return this;
    }
    public String get(){
      return msg_;
    }
    protected String getPre(){
      return msg_pre_;
    }
    public void active(boolean active){
      this.active_ = active;
    }
    public boolean isActive(){
      return this.active_;
    }
  }
}