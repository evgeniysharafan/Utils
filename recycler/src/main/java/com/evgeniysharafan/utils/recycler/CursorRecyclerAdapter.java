package com.evgeniysharafan.utils.recycler;

import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.widget.Filter;
import android.widget.FilterQueryProvider;
import android.widget.Filterable;

/**
 * Provide a {@link android.support.v7.widget.RecyclerView.Adapter} implementation with cursor
 * support.
 * <p/>
 * Child classes only need to implement {@link #onCreateViewHolder(android.view.ViewGroup, int)} and
 * {@link #onBindViewHolderCursor(android.support.v7.widget.RecyclerView.ViewHolder, Cursor)}.
 * <p/>
 * This class does not implement deprecated fields and methods from CursorAdapter! Incidentally,
 * only {@link android.widget.CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER} is available, so the
 * flag is implied, and only the Adapter behavior using this flag has been ported.
 *
 * @author Shywim, edited by Jeevuz (vasili.chyrvon@gmail.com)
 * @see <a href=https://gist.github.com/Shywim/127f207e7248fe48400b>Shyvim's gist</a>
 * @see android.widget.CursorAdapter
 * @see android.support.v7.widget.RecyclerView.Adapter
 * @see Filterable
 */
@SuppressWarnings("unused")
public abstract class CursorRecyclerAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> implements Filterable, CursorFilter.CursorFilterClient {

    private boolean mDataValid;
    private int mRowIDColumn;
    private Cursor mCursor;
    private ChangeObserver mChangeObserver;
    private DataSetObserver mDataSetObserver;
    private CursorFilter mCursorFilter;
    private FilterQueryProvider mFilterQueryProvider;

    public CursorRecyclerAdapter(Cursor cursor) {
        init(cursor);
    }

    void init(Cursor c) {
        boolean cursorPresent = c != null;
        mCursor = c;
        mDataValid = cursorPresent;
        mRowIDColumn = cursorPresent ? c.getColumnIndexOrThrow("_id") : -1;

        mChangeObserver = new ChangeObserver();
        mDataSetObserver = new MyDataSetObserver();

        if (cursorPresent) {
            if (mChangeObserver != null) c.registerContentObserver(mChangeObserver);
            if (mDataSetObserver != null) c.registerDataSetObserver(mDataSetObserver);
        }
    }

    /**
     * This method will move the Cursor to the correct position and call
     * {@link #onBindViewHolderCursor(android.support.v7.widget.RecyclerView.ViewHolder,
     * Cursor)}.
     *
     * @param holder {@inheritDoc}
     * @param i      {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(VH holder, int i) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(i)) {
            throw new IllegalStateException("couldn't move cursor to position " + i);
        }
        onBindViewHolderCursor(holder, mCursor);
    }

    /**
     * See {@link android.widget.CursorAdapter#bindView(android.view.View, android.content.Context,
     * Cursor)},
     * {@link #onBindViewHolder(android.support.v7.widget.RecyclerView.ViewHolder, int)}
     *
     * @param holder View holder.
     * @param cursor The cursor from which to get the data. The cursor is already
     *               moved to the correct position.
     */
    public abstract void onBindViewHolderCursor(VH holder, Cursor cursor);

    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    /**
     * This is not required by RecyclerView.Adaper, but is useful sometime
     *
     * @see android.widget.ListAdapter#getItem(int)
     */
    public Object getItem(int position) {
        if (mDataValid && mCursor != null) {
            mCursor.moveToPosition(position);
            return mCursor;
        } else {
            return null;
        }
    }

    /**
     * Get the row id associated with the specified position in the list. If {@link #hasStableIds()}
     * would return false, Cursor is null or empty, or data is not valid, this method would return
     * {@link android.support.v7.widget.RecyclerView#NO_ID}
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return id The id of the item at the specified position or {@link android.support.v7.widget.RecyclerView#NO_ID}
     * @see android.widget.ListAdapter#getItemId(int)
     * @see android.support.v7.widget.RecyclerView.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        if (hasStableIds() && mDataValid && mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getLong(mRowIDColumn);
        } else {
            return RecyclerView.NO_ID;
        }
    }

    public Cursor getCursor() {
        return mCursor;
    }

    /**
     * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
     * closed.
     *
     * @param cursor The new cursor to be used
     */
    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.  Unlike
     * {@link #changeCursor(Cursor)}, the returned old Cursor is <em>not</em>
     * closed.
     *
     * @param newCursor The new cursor to be used.
     * @return Returns the previously set Cursor, or null if there wasa not one.
     * If the given new Cursor is the same instance is the previously set
     * Cursor, null is also returned.
     */
    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        int oldItemCount = getItemCount();
        if (oldCursor != null) {
            if (mChangeObserver != null) oldCursor.unregisterContentObserver(mChangeObserver);
            if (mDataSetObserver != null) oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        mCursor = newCursor;
        if (newCursor != null) {
            if (mChangeObserver != null) newCursor.registerContentObserver(mChangeObserver);
            if (mDataSetObserver != null) newCursor.registerDataSetObserver(mDataSetObserver);
            mRowIDColumn = newCursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
            // notify the observers about the new cursor
            notifyDataSetChanged();
        } else {
            mRowIDColumn = -1;
            mDataValid = false;
            // notify the observers about the lack of a data set
            notifyItemRangeRemoved(0, oldItemCount);
        }
        return oldCursor;
    }

    /**
     * <p>Converts the cursor into a CharSequence. Subclasses should override this
     * method to convert their results. The default implementation returns an
     * empty String for null values or the default String representation of
     * the value.</p>
     *
     * @param cursor the cursor to convert to a CharSequence
     * @return a CharSequence representing the value
     */
    public CharSequence convertToString(Cursor cursor) {
        return cursor == null ? "" : cursor.toString();
    }

    /**
     * Runs a query with the specified constraint. This query is requested
     * by the filter attached to this adapter.
     * <p/>
     * The query is provided by a
     * {@link FilterQueryProvider}.
     * If no provider is specified, the current cursor is not filtered and returned.
     * <p/>
     * After this method returns the resulting cursor is passed to {@link #changeCursor(Cursor)}
     * and the previous cursor is closed.
     * <p/>
     * This method is always executed on a background thread, not on the
     * application's main thread (or UI thread.)
     * <p/>
     * Contract: when constraint is null or empty, the original results,
     * prior to any filtering, must be returned.
     *
     * @param constraint the constraint with which the query must be filtered
     * @return a Cursor representing the results of the new query
     * @see #getFilter()
     * @see #getFilterQueryProvider()
     * @see #setFilterQueryProvider(FilterQueryProvider)
     */
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (mFilterQueryProvider != null) {
            return mFilterQueryProvider.runQuery(constraint);
        }

        return mCursor;
    }

    public Filter getFilter() {
        if (mCursorFilter == null) {
            mCursorFilter = new CursorFilter(this);
        }
        return mCursorFilter;
    }

    /**
     * Returns the query filter provider used for filtering. When the
     * provider is null, no filtering occurs.
     *
     * @return the current filter query provider or null if it does not exist
     * @see #setFilterQueryProvider(FilterQueryProvider)
     * @see #runQueryOnBackgroundThread(CharSequence)
     */
    public FilterQueryProvider getFilterQueryProvider() {
        return mFilterQueryProvider;
    }

    /**
     * Sets the query filter provider used to filter the current Cursor.
     * The provider's
     * {@link FilterQueryProvider#runQuery(CharSequence)}
     * method is invoked when filtering is requested by a client of
     * this adapter.
     *
     * @param filterQueryProvider the filter query provider or null to remove it
     * @see #getFilterQueryProvider()
     * @see #runQueryOnBackgroundThread(CharSequence)
     */
    public void setFilterQueryProvider(FilterQueryProvider filterQueryProvider) {
        mFilterQueryProvider = filterQueryProvider;
    }

    /**
     * Called when the {@link ContentObserver} on the cursor receives a change notification.
     * Can be implemented by sub-class.
     *
     * @see ContentObserver#onChange(boolean)
     */
    protected void onContentChanged() {

    }

    private class ChangeObserver extends ContentObserver {
        public ChangeObserver() {
            super(new Handler());
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            onContentChanged();
        }
    }

    private class MyDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            int itemCount = getItemCount();
            mDataValid = false;
            // data vas invalidated and no longer available
            notifyItemRangeRemoved(0, itemCount);
        }
    }

    /**
     * <p>The CursorFilter delegates most of the work to the CursorAdapter.
     * Subclasses should override these delegate methods to run the queries
     * and convert the results into String that can be used by auto-completion
     * widgets.</p>
     */

}

class CursorFilter extends Filter {

    CursorFilterClient mClient;

    interface CursorFilterClient {
        CharSequence convertToString(Cursor cursor);

        Cursor runQueryOnBackgroundThread(CharSequence constraint);

        Cursor getCursor();

        void changeCursor(Cursor cursor);
    }

    CursorFilter(CursorFilterClient client) {
        mClient = client;
    }

    @Override
    public CharSequence convertResultToString(Object resultValue) {
        return mClient.convertToString((Cursor) resultValue);
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        Cursor cursor = mClient.runQueryOnBackgroundThread(constraint);

        FilterResults results = new FilterResults();
        if (cursor != null) {
            results.count = cursor.getCount();
            results.values = cursor;
        } else {
            results.count = 0;
            results.values = null;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        Cursor oldCursor = mClient.getCursor();

        if (results.values != null && results.values != oldCursor) {
            mClient.changeCursor((Cursor) results.values);
        }
    }

}