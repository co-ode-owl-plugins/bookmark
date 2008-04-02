package org.coode.bookmark;

import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.OWLEntityComparator;
import org.semanticweb.owl.model.*;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.*;

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
public class BookmarkModel implements ListModel {

    private Map<OWLOntology, OntologyBookmarks> ontologybookmarks = new HashMap<OWLOntology, OntologyBookmarks>();

    private OWLModelManager mngr;

    private List<ListDataListener> listeners = new ArrayList<ListDataListener>();

    private OWLOntologyChangeListener ontListener = new OWLOntologyChangeListener(){
        public void ontologiesChanged(List<? extends OWLOntologyChange> changes) throws OWLException {
            refill();
        }
    };

    private OWLModelManagerListener modelListener = new OWLModelManagerListener(){
        public void handleChange(OWLModelManagerChangeEvent event) {
            if (event.isType(EventType.ACTIVE_ONTOLOGY_CHANGED)){
                try {
                    refill();
                }
                catch (OWLException e) {
                    Logger.getLogger(BookmarkModel.class).error(e);
                }
            }
        }
    };

    public BookmarkModel(OWLModelManager owlModelManager) {
        super();

        mngr = owlModelManager;

        owlModelManager.addOntologyChangeListener(ontListener);

        owlModelManager.addListener(modelListener);

        try {
            refill();
        }
        catch (OWLException e) {
            Logger.getLogger(BookmarkModel.class).error(e);
        }
    }

    /**
     * Always add to the active ontology bookmark
     * @param obj
     * @throws OWLException
     */
    public void add(OWLEntity obj) throws OWLException {
        OWLOntology ont = mngr.getActiveOntology();
        List<OWLOntologyChange> changes = ontologybookmarks.get(ont).add(obj);
        if (!changes.isEmpty()){
            mngr.applyChanges(changes);
            refill(ont);
        }
    }

    /**
     * Always remove from all ontologies' bookmark
     * @param obj
     * @throws OWLException
     */
    public void remove(OWLEntity obj) throws OWLException {
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
        Collections.sort(valueList, new OWLEntityComparator<OWLEntity>(mngr));
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


    private void refill() throws OWLException {
        ontologybookmarks.clear();
        for (OWLOntology ont : mngr.getOntologies()){
            OntologyBookmarks bms = new OntologyBookmarks(mngr.getOWLOntologyManager(), ont);
            ontologybookmarks.put(ont, bms);
        }
        fireDataChanged();
    }

    private void refill(OWLOntology ont) throws OWLException {
        ontologybookmarks.remove(ont);
        ontologybookmarks.put(ont, new OntologyBookmarks(mngr.getOWLOntologyManager(), ont));
        fireDataChanged();
    }

    public void dispose(){
        listeners.clear();
        mngr.removeListener(modelListener);
        mngr.removeOntologyChangeListener(ontListener);
    }


    public boolean contains(OWLEntity entity) {
        for (OntologyBookmarks bm : ontologybookmarks.values()){
            if (bm.getBookmarks().contains(entity)){
                return true;
            }
        }
        return false;
    }
}
