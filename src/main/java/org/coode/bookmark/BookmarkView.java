package org.coode.bookmark;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.protege.editor.core.ui.view.DisposableAction;
import org.protege.editor.owl.ui.OWLIcons;
import org.protege.editor.owl.ui.view.AbstractOWLSelectionViewComponent;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
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
public class BookmarkView extends AbstractOWLSelectionViewComponent {
	private static final long serialVersionUID = -8559594691278332112L;

	protected BookmarkList list;

    protected DisposableAction deleteAction = new DisposableAction("Remove Bookmark", OWLIcons.getIcon("class.delete.png")){
		private static final long serialVersionUID = 1L;

		@Override
        public void dispose() {
        }

        public void actionPerformed(ActionEvent actionEvent) {
            for (OWLObject obj : list.getSelectedObjects()){
                try {
                    if (obj instanceof OWLEntity){
                        list.getBookmarkModel().remove((OWLEntity)obj);
                    }
                }
                catch (OWLRuntimeException e) {
                    Logger.getLogger(BookmarkView.class).error(e);
                }
            }
        }
    };

    
    private ListSelectionListener listSelectionListener = new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
            final Object val = list.getSelectedValue();
            deleteAction.setEnabled(val != null);
            if (val != null){
                getOWLEditorKit().getWorkspace().getOWLSelectionModel().setSelectedEntity((OWLEntity)val);
            }
        }
    };


    @Override
    public void initialiseView() {
        setLayout(new BorderLayout());

        list = new BookmarkList(getOWLEditorKit());

        list.getSelectionModel().addListSelectionListener(listSelectionListener);

        add(new JScrollPane(list), BorderLayout.CENTER);

        deleteAction.setEnabled(false);

        addAction(deleteAction, "A", "A");
    }


    @Override
    public void disposeView() {
        list.getSelectionModel().removeListSelectionListener(listSelectionListener);
        list.getBookmarkModel().dispose();
    }


    @Override
    protected OWLObject updateView() {
        OWLEntity selectedEntity = getOWLWorkspace().getOWLSelectionModel().getSelectedEntity();
        if (list.getSelectedObjects().contains(selectedEntity)){
            return selectedEntity;
        }
        else if (list.getBookmarkModel().contains(selectedEntity)){
            list.setSelectedValue(selectedEntity, true);
            return selectedEntity;
        }
        list.clearSelection();
        return null;
    }


    @Override
    protected boolean isOWLClassView() {
        return true;
    }


    @Override
    protected boolean isOWLObjectPropertyView() {
        return true;
    }


    @Override
    protected boolean isOWLDataPropertyView() {
        return true;
    }


    @Override
    protected boolean isOWLIndividualView() {
        return true;
    }


    @Override
    protected boolean isOWLAnnotationPropertyView() {
        return true;
    }


    @Override
    protected boolean isOWLDatatypeView() {
        return true;
    }
}
