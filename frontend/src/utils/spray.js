export function toDateKey(value) {
  if (!value) return '';
  if (typeof value === 'string') return value.slice(0, 10);
  return String(value).slice(0, 10);
}

/** Days since last activity of type; -1 if never logged */
export function daysSinceLastActivity(activities, type) {
  const target = (type || '').toLowerCase();
  let latest = null;
  for (const a of activities || []) {
    if ((a.activityType || '').toLowerCase() !== target) continue;
    const key = toDateKey(a.activityDate);
    if (!key) continue;
    if (!latest || key > latest) latest = key;
  }
  if (!latest) return -1;
  const last = new Date(`${latest}T12:00:00`);
  const today = new Date();
  today.setHours(12, 0, 0, 0);
  return Math.floor((today - last) / (1000 * 60 * 60 * 24));
}

export function fungicideOverdue(days) {
  return days === -1 || days > 10;
}
