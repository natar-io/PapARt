/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.graph;

import java.util.ArrayList;
import java.util.Collection;
import processing.core.PMatrix3D;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author jiii
 */
public abstract class Node {

    private Node root;
    private final ArrayList<Node> children = new ArrayList<Node>();
    private Node parent;
    private final PMatrix3D transform = new PMatrix3D();

    public Node getRoot() {
        return root;
    }

    public Node getParent() {
        return this.parent;
    }

    public boolean isRoot() {
        return this == root;
    }

    protected void updateRootOf(Node n) {
        n.root = this.root;
    }
    
    public void setRoot(){
        this.root = this;
    }

    /**
     *
     * @return all the children
     */
    public Collection<Node> getChildren() {
        return children;
    }

    /**
     *
     * @param child to add
     */
    public void addChild(Node child) {
        children.add(child);
        updateRootOf(child);
    }

    public void setTransform(PMatrix3D mat) {
        this.transform.set(mat);
    }

    public PMatrix3D getTransform() {
        return this.transform.get();
    }

    public PMatrix3D getAbsoluteTrasform() {
        Node currentNode = this;
        PMatrix3D currentMat = new PMatrix3D();
        currentMat.set(this.transform);

        while (!currentNode.isRoot()) {
            currentNode = currentNode.getParent();
            currentMat.apply(currentNode.transform);
        }

        return currentMat;
    }

}
