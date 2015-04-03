package org.coode.bookmark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLRuntimeException;

/*
 * Copyright (C) 2007, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Oct 5, 2006<br><br>
 * <p/>
 */
public class BookmarkModel implements ListModel<OWLEntity> {

    private Map<OWLOntology, OntologyBookmarks> ontologybookmarks = new HashMap<OWLOntology, OntologyBookmarks>();

    private OWLModelManager mngr;

    private List<ListDataListener> listeners = new ArrayList<ListDataListener>();

    private OWLOntologyChangeListener ontListener = new OWLOntologyChangeListener(){
        public void ontologiesChanged(List<? extends OWLOntologyChange> changes) {
            refill();
        }
    };

    private OWLModelManagerListener modelListener = new OWLModelManagerListener(){
        public void handleChange(OWLModelManagerChangeEvent event) {
            if (event.isType(EventType.ACTIVE_ONTOLOGY_CHANGED)){
                try {
                    refill();
                }
                catch (OWLRuntimeException e) {
                    Logger.getLogger(BookmarkModel.class).error(e);
                }
            }
        }
    };

    protected BookmarkModel(OWLModelManager owlModelManager) {
        super();

        mngr = owlModelManager;

        owlModelManager.addOntologyChangeListener(ontListener);

        owlModelManager.addListener(modelListener);

        try {
            refill();
        }
        catch (OWLRuntimeException e) {
            Logger.getLogger(BookmarkModel.class).error(e);
        }
    }

    /**
     * Always add to the active ontology bookmark
     * @param obj object to add
     */
    public void add(OWLEntity obj) {
        OWLOntology ont = mngr.getActiveOntology();
        List<OWLOntologyChange> changes = ontologybookmarks.get(ont).add(obj);
        if (!changes.isEmpty()){
            mngr.applyChanges(changes);
            refill(ont);
        }
    }

    /**
     * Always remove from all ontologies' bookmark
     * @param obj object to remove
     */
    public void remove(OWLEntity obj) {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        for (OntologyBookmarks bm : ontologybookmarks.values()){
            changes.addAll(bm.remove(obj));
        }
        if (!changes.isEmpty()){
            mngr.applyChanges(changes);
            refill();
        }
    }

    public int getSize() {
        int rowcount = 0;
        for (OntologyBookmarks bms : ontologybookmarks.values()){
            rowcount += bms.getSize();
        }
        return rowcount;
    }

    public OWLEntity getElementAt(int i) {
        Set<OWLEntity> valueSet = new HashSet<OWLEntity>();
        for (OntologyBookmarks bms : ontologybookmarks.values()){
            valueSet.addAll(bms.getBookmarks());
        }
        List<OWLEntity> valueList = new ArrayList<OWLEntity>(valueSet);
        Collections.sort(valueList);
        return valueList.get(i);
    }

    private void fireDataChanged() {
        for (ListDataListener l : listeners){
            l.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSize()-1));
        }
    }
    
    public void addListDataListener(ListDataListener listDataListener) {
        listeners.add(listDataListener);
    }

    public void removeListDataListener(ListDataListener listDataListener) {
        listeners.remove(listDataListener);
    }


    protected void refill() {
        ontologybookmarks.clear();
        for (OWLOntology ont : mngr.getActiveOntologies()){
            OntologyBookmarks bms = new OntologyBookmarks(mngr.getOWLOntologyManager(), ont);
            ontologybookmarks.put(ont, bms);
        }
        fireDataChanged();
    }

    private void refill(OWLOntology ont) {
        ontologybookmarks.remove(ont);
        ontologybookmarks.put(ont, new OntologyBookmarks(mngr.getOWLOntologyManager(), ont));
        fireDataChanged();
    }

    /**
     * Dispose the object: remove listeners.
     */
    public void dispose(){
        listeners.clear();
        mngr.removeListener(modelListener);
        mngr.removeOntologyChangeListener(ontListener);
    }


    /**
     * @param entity entity to check
     * @return true if the entity is contained in the bookmarks
     */
    public boolean contains(OWLEntity entity) {
        for (OntologyBookmarks bm : ontologybookmarks.values()){
            if (bm.getBookmarks().contains(entity)){
                return true;
            }
        }
        return false;
    }
}
