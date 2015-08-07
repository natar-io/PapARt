
ArrayList<SubSketch> subSketches = new ArrayList<SubSketch>();

List<Class<? extends SubSketch>> subSketchList = new ArrayList<Class<? extends SubSketch>>();


void initSubSketches(){


    Class<?>[] classes =  SubSketch.class.getClasses();

    subSketchList.add(Empathy.class);
    subSketchList.add(FireDrag.class);
    subSketchList.add(RecursiveTree.class);

    subSketchList.add(APP.class);
    subSketchList.add(Snow.class);
    subSketchList.add(CarWheel.class);
    subSketchList.add(BackgroundApp.class);


}

    // Empathy empathy;
    // FireDrag fireDrag;
    // RecursiveTree recTree;

