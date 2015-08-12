static final float meshScale = 120f;

/////// 3D models ////////
OBJModelLite modelLite;
OBJModel model59_80 = null;
OBJModel model60_80 = null;

GLModel sphereModel = null;




/////////// Shadow map variables /////////
GLShadowMap shadowMap;
GLGraphicsOffscreenDepth depthScreen;
GLTextureDepth depthTex;
float[] matF;
int texLocation, shadowLocation;


////////// Light Shader variables /////////
GLSLShader lightShader;
int texLocation2;
GLModelEffect lightEffect;


void initScene3D(PApplet parent, String modelPath){
    //   initShadowMap(this, myModel);

    loadShaders(parent);
    load3DModels(parent, modelPath);

    sphereModel = loadMesh(sketchPath + "/data/models/sphere.stl", 1);
}


void loadShaders(PApplet parent){

    // TODO: ?? useless ??
    lightEffect = new GLModelEffect(parent, "shaders/dirLightAmbDiffSpecPix.xml");

    lightShader= new GLSLShader(parent, "shaders/capSc.vert", "shaders/capSc.frag");

    lightShader.start();
      texLocation2 = lightShader.getUniformLocation("tex");
    lightShader.stop();
}


BoundingBox boundingBox;

void load3DModels(PApplet parent, String modelPath){

    println("Begin Loading of 3D model");

    model59_80 = new OBJModel(parent, sketchPath + "/bordeaux/tile_x59y80.obj", 
			   "absolute", TRIANGLES);
    model59_80.setupGL();

    boundingBox = new BoundingBox(this, model59_80);

    model60_80 = new OBJModel(parent, sketchPath + "/bordeaux/tile_x60y80.obj", 
    			   "absolute", TRIANGLES);
    model60_80.setupGL();
    

    
}



// TODO: use Shadow Map ?

GLSLShader shadowMapShader;

void initShadowMap(PApplet parent, boolean useTex){

    //  depthScreen = new GLGraphicsOffscreenDepth(parent, 512, 512);
    depthScreen = new GLGraphicsOffscreenDepth(parent, 1024, 1024);

    depthTex = (GLTextureDepth) depthScreen.getTexture();
    matF = new float[16];


    if(useTex)
      shadowMapShader = new GLSLShader(parent, sketchPath + "/data/shaders/shadowMap.vert.backup",
				        sketchPath + "/data/shaders/shadowMap.frag.backup");
    else
      shadowMapShader = new GLSLShader(parent,  sketchPath + "/data/shaders/shadowMap.vert.backup", 
				        sketchPath + "/data/shaders/shadowMap-noTex.frag.backup");

    shadowMapShader.start();
    texLocation = shadowMapShader.getUniformLocation("tex");
    shadowLocation = shadowMapShader.getUniformLocation("ShadowMap");
    depthTex.setTexUniform(shadowLocation);
    shadowMapShader.stop();

}





GLModel loadMesh(String path, float meshScale){

    println("loading mesh : " + path);

    // loading the deer without the texture...
    TriangleMesh  mesh=(TriangleMesh)new STLReader().loadBinary(dataPath(path),STLReader.TRIANGLEMESH);
     
    Sphere bSphere = mesh.getBoundingSphere(); 
     
    mesh.translate(new Vec3D(-bSphere.x(), -bSphere.y(), -bSphere.z()));
    
    mesh.scale( 1f / bSphere.radius);
    //    mesh.translate(new Vec3D(.0, .0, 0));
    mesh.scale(meshScale);

    // println("radius"  + bSphere.radius);
    // println(bSphere);
    
    mesh.computeVertexNormals();
    // get flattened vertex array
    float[] verts=mesh.getMeshAsVertexArray();
    // in the array each vertex has 4 entries (XYZ + 1 spacing)
    int numV=verts.length/4;  
    float[] norms=mesh.getVertexNormalsAsArray();
    
    //  cubeSphere = new GLModel(allProjCam[0].paperScreen, numV, TRIANGLES, GLModel.STATIC);
    GLModel outModel = new GLModel(this, numV, TRIANGLES, GLModel.STATIC);
    outModel.beginUpdateVertices();
    for (int i = 0; i < numV; i++) {
	outModel.updateVertex(i, verts[4 * i], verts[4 * i + 1], verts[4 * i + 2]);
    }
    outModel.endUpdateVertices(); 
    
    outModel.initNormals();
    outModel.beginUpdateNormals();
    for (int i = 0; i < numV; i++) 
	outModel.updateNormal(i, norms[4 * i], norms[4 * i + 1], norms[4 * i + 2]);
    outModel.endUpdateNormals();  
    
    // Setting the color of all vertices to green, but not used, see comments in the draw() method.
    outModel.initColors();
    outModel.beginUpdateColors();
    for (int i = 0; i < numV; i++) outModel.updateColor(i, 200, 200, 200, 225);
    outModel.endUpdateColors(); 
    // Setting model shininess.
    outModel.setShininess(40);
    
    // outModel.initTextures(1);
    // outModel.setTexture(0, litSphereTexStatue);
    
    return outModel;
 }



