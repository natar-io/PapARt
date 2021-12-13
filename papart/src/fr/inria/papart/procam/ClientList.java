/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Jeremy Laviole
 */
public class ClientList extends ArrayList<HashMap<String, String>> {

    public ClientList(String clientList) {
        super();
        String[] clients = clientList.split("\\r?\\n");
        for (String c : clients) {
            HashMap<String, String> map = new HashMap<>();
            for (String element : c.split(" ")) {
                String[] el = element.split("=");
                if (el.length > 1) {
                    map.put(el[0], el[1]);
                }
            }
            if (map.containsKey("name")) {
                System.out.println("Client: " + map.get("name"));
            }
            this.add(map);
        }
    }

    public boolean hasClient(String name) {
        boolean found = false;
        for (HashMap<String, String> map : this) {
            if (map.containsKey("name")) {
                found = found || map.get("name").startsWith(name);
            }
        }
        return found;
    }
}
