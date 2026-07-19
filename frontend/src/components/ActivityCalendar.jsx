import { useMemo, useState, useEffect } from 'react';
import { toDateKey } from '../utils/spray';

const MONTH_NAMES = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December',
];

const WEEKDAYS = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
const FILTER_TYPES = ['all', 'fertilizer', 'pesticide', 'fungicide', 'irrigation'];

function formatYmd(date) {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

function buildMonthCells(year, month) {
  const first = new Date(year, month, 1);
  let dow = first.getDay();
  dow = (dow + 6) % 7;
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const cells = [];
  for (let i = 0; i < dow; i += 1) cells.push(null);
  for (let day = 1; day <= daysInMonth; day += 1) {
    cells.push(new Date(year, month, day));
  }
  while (cells.length % 7 !== 0) cells.push(null);
  return cells;
}

function typeClass(type) {
  const t = (type || '').toLowerCase();
  if (t === 'fertilizer') return 'cal-dot--fertilizer';
  if (t === 'pesticide') return 'cal-dot--pesticide';
  if (t === 'fungicide') return 'cal-dot--fungicide';
  if (t === 'irrigation') return 'cal-dot--irrigation';
  return 'cal-dot--other';
}

function pillClass(type) {
  const t = (type || '').toLowerCase();
  if (t === 'fertilizer') return 'cal-pill--fertilizer';
  if (t === 'pesticide') return 'cal-pill--pesticide';
  if (t === 'fungicide') return 'cal-pill--fungicide';
  if (t === 'irrigation') return 'cal-pill--irrigation';
  return 'cal-pill--other';
}

function matchesFilter(activity, filter) {
  if (filter === 'all') return true;
  return (activity.activityType || '').toLowerCase() === filter;
}

export default function ActivityCalendar({
  activities,
  fieldId,
  onDeleteActivity,
  onEditActivity,
  deletingId,
  savingId,
  disabled,
}) {
  const today = new Date();
  const [viewYear, setViewYear] = useState(today.getFullYear());
  const [viewMonth, setViewMonth] = useState(today.getMonth());
  const [selectedKey, setSelectedKey] = useState(null);
  const [typeFilter, setTypeFilter] = useState('all');
  const [editingId, setEditingId] = useState(null);
  const [editForm, setEditForm] = useState(null);

  useEffect(() => {
    setSelectedKey(null);
    setEditingId(null);
    setEditForm(null);
    const now = new Date();
    setViewYear(now.getFullYear());
    setViewMonth(now.getMonth());
    setTypeFilter('all');
  }, [fieldId]);

  const filteredActivities = useMemo(
    () => (activities || []).filter((a) => matchesFilter(a, typeFilter)),
    [activities, typeFilter]
  );

  const byDate = useMemo(() => {
    const map = new Map();
    for (const a of filteredActivities) {
      const key = toDateKey(a.activityDate);
      if (!key) continue;
      if (!map.has(key)) map.set(key, []);
      map.get(key).push(a);
    }
    return map;
  }, [filteredActivities]);

  const cells = useMemo(
    () => buildMonthCells(viewYear, viewMonth),
    [viewYear, viewMonth]
  );

  const goPrev = () => {
    setSelectedKey(null);
    if (viewMonth === 0) {
      setViewMonth(11);
      setViewYear((y) => y - 1);
    } else {
      setViewMonth((m) => m - 1);
    }
  };

  const goNext = () => {
    setSelectedKey(null);
    if (viewMonth === 11) {
      setViewMonth(0);
      setViewYear((y) => y + 1);
    } else {
      setViewMonth((m) => m + 1);
    }
  };

  const goToday = () => {
    const now = new Date();
    setViewYear(now.getFullYear());
    setViewMonth(now.getMonth());
    setSelectedKey(formatYmd(now));
  };

  const startEdit = (a) => {
    setEditingId(a.id);
    setEditForm({
      activityDate: toDateKey(a.activityDate),
      activityType: a.activityType || 'fungicide',
      inputName: a.inputName || '',
      notes: a.notes || '',
    });
  };

  const cancelEdit = () => {
    setEditingId(null);
    setEditForm(null);
  };

  const submitEdit = async (e) => {
    e.preventDefault();
    if (!editingId || !editForm || !onEditActivity) return;
    await onEditActivity(editingId, editForm);
    cancelEdit();
  };

  const selectedList = selectedKey ? byDate.get(selectedKey) || [] : [];

  if (!fieldId) {
    return (
      <div className="activity-calendar activity-calendar--empty">
        <p className="empty">Select a field to view the activity calendar.</p>
      </div>
    );
  }

  return (
    <div className="activity-calendar">
      <div className="cal-header">
        <h3 className="cal-title">Input history calendar</h3>
        <p className="field-meta cal-sub">
          Fertilizer, pesticide, fungicide, and irrigation logged by day for this field.
        </p>
      </div>

      <div className="cal-filter-chips" role="group" aria-label="Filter activity type">
        {FILTER_TYPES.map((f) => (
          <button
            key={f}
            type="button"
            className={`filter-chip ${typeFilter === f ? 'active' : ''}`}
            onClick={() => {
              setTypeFilter(f);
              setSelectedKey(null);
            }}
          >
            {f === 'all' ? 'All' : f}
          </button>
        ))}
      </div>

      <div className="cal-nav">
        <button type="button" className="btn btn-ghost btn-sm" onClick={goPrev}>
          ← Prev
        </button>
        <span className="cal-month-label">
          {MONTH_NAMES[viewMonth]} {viewYear}
        </span>
        <button type="button" className="btn btn-ghost btn-sm" onClick={goNext}>
          Next →
        </button>
        <button type="button" className="btn btn-secondary btn-sm" onClick={goToday}>
          Today
        </button>
      </div>

      <div className="cal-legend">
        <span className="cal-legend-item">
          <span className="cal-dot cal-dot--fertilizer" /> Fertilizer
        </span>
        <span className="cal-legend-item">
          <span className="cal-dot cal-dot--pesticide" /> Pesticide
        </span>
        <span className="cal-legend-item">
          <span className="cal-dot cal-dot--fungicide" /> Fungicide
        </span>
        <span className="cal-legend-item">
          <span className="cal-dot cal-dot--irrigation" /> Irrigation
        </span>
      </div>

      <div className="cal-weekdays">
        {WEEKDAYS.map((d) => (
          <div key={d} className="cal-weekday">
            {d}
          </div>
        ))}
      </div>

      <div className="cal-grid">
        {cells.map((cell, idx) => {
          if (!cell) {
            return <div key={`e-${idx}`} className="cal-cell cal-cell--empty" />;
          }
          const key = formatYmd(cell);
          const dayActs = byDate.get(key) || [];
          const isSelected = selectedKey === key;
          const isToday = key === formatYmd(today);
          return (
            <button
              key={key}
              type="button"
              className={`cal-cell ${dayActs.length ? 'cal-cell--has' : ''} ${isSelected ? 'cal-cell--selected' : ''} ${isToday ? 'cal-cell--today' : ''}`}
              aria-pressed={isSelected}
              aria-label={`${cell.getDate()} ${MONTH_NAMES[viewMonth]}, ${dayActs.length} activities`}
              onClick={() => setSelectedKey((prev) => (prev === key ? null : key))}
            >
              <span className="cal-day-num">{cell.getDate()}</span>
              {dayActs.length > 0 && (
                <span className="cal-dots">
                  {[...new Set(dayActs.map((a) => (a.activityType || '').toLowerCase()))].map(
                    (t) => (
                      <span key={t} className={`cal-dot ${typeClass(t)}`} title={t} />
                    )
                  )}
                </span>
              )}
            </button>
          );
        })}
      </div>

      {selectedKey && (
        <div className="cal-detail">
          <h4 className="cal-detail-title">
            {selectedKey}
            <span className="field-meta"> — {selectedList.length} record(s)</span>
          </h4>
          {selectedList.length === 0 ? (
            <p className="empty">No activities of this type on this day.</p>
          ) : (
            <ul className="cal-detail-list">
              {selectedList.map((a) => (
                <li key={a.id} className="cal-detail-row">
                  {editingId === a.id && editForm ? (
                    <form className="cal-edit-form" onSubmit={submitEdit}>
                      <label>
                        Date
                        <input
                          type="date"
                          required
                          disabled={disabled}
                          value={editForm.activityDate}
                          onChange={(e) =>
                            setEditForm({ ...editForm, activityDate: e.target.value })
                          }
                        />
                      </label>
                      <label>
                        Type
                        <select
                          disabled={disabled}
                          value={editForm.activityType}
                          onChange={(e) =>
                            setEditForm({ ...editForm, activityType: e.target.value })
                          }
                        >
                          <option value="fungicide">fungicide</option>
                          <option value="pesticide">pesticide</option>
                          <option value="fertilizer">fertilizer</option>
                          <option value="irrigation">irrigation</option>
                        </select>
                      </label>
                      <label>
                        Input
                        <input
                          disabled={disabled}
                          value={editForm.inputName}
                          onChange={(e) =>
                            setEditForm({ ...editForm, inputName: e.target.value })
                          }
                        />
                      </label>
                      <label>
                        Notes
                        <textarea
                          disabled={disabled}
                          value={editForm.notes}
                          onChange={(e) =>
                            setEditForm({ ...editForm, notes: e.target.value })
                          }
                        />
                      </label>
                      <div className="cal-edit-actions">
                        <button
                          type="button"
                          className="btn btn-secondary btn-sm"
                          onClick={cancelEdit}
                          disabled={disabled}
                        >
                          Cancel
                        </button>
                        <button
                          type="submit"
                          className="btn btn-primary btn-sm"
                          disabled={disabled || savingId === a.id}
                        >
                          {savingId === a.id ? 'Saving…' : 'Save'}
                        </button>
                      </div>
                    </form>
                  ) : (
                    <>
                      <div>
                        <span className={`cal-type-pill ${pillClass(a.activityType)}`}>
                          {a.activityType}
                        </span>
                        {a.inputName && (
                          <span className="cal-detail-input"> · {a.inputName}</span>
                        )}
                        {a.notes && <p className="cal-detail-notes">{a.notes}</p>}
                      </div>
                      <div className="cal-row-actions">
                        {onEditActivity && (
                          <button
                            type="button"
                            className="btn btn-ghost btn-sm"
                            disabled={disabled}
                            onClick={() => startEdit(a)}
                          >
                            Edit
                          </button>
                        )}
                        {onDeleteActivity && (
                          <button
                            type="button"
                            className="btn btn-danger-ghost btn-sm"
                            disabled={disabled || deletingId === a.id}
                            onClick={() => onDeleteActivity(a.id)}
                          >
                            {deletingId === a.id ? '…' : 'Delete'}
                          </button>
                        )}
                      </div>
                    </>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}
