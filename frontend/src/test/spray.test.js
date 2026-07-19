import { describe, it, expect } from 'vitest';
import { toDateKey, daysSinceLastActivity, fungicideOverdue } from '../utils/spray';

describe('toDateKey', () => {
  it('returns empty string for null', () => {
    expect(toDateKey(null)).toBe('');
  });

  it('returns empty string for undefined', () => {
    expect(toDateKey(undefined)).toBe('');
  });

  it('trims ISO string to first 10 chars', () => {
    expect(toDateKey('2026-06-15T12:00:00')).toBe('2026-06-15');
  });

  it('returns same string if already short', () => {
    expect(toDateKey('2026-06-15')).toBe('2026-06-15');
  });

  it('converts number to string', () => {
    expect(toDateKey(20260615)).toBe('20260615');
  });
});

describe('daysSinceLastActivity', () => {
  const today = new Date();
  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);
  const yKey = yesterday.toISOString().slice(0, 10);

  const activities = [
    { activityType: 'Fertilizer', activityDate: '2026-06-01' },
    { activityType: 'fungicide', activityDate: yKey },
    { activityType: 'Fungicide', activityDate: '2026-06-10' },
  ];

  it('returns days since most recent matching type', () => {
    const days = daysSinceLastActivity(activities, 'fungicide');
    expect(days).toBe(1);
  });

  it('returns -1 when no matching type exists', () => {
    const days = daysSinceLastActivity(activities, 'pesticide');
    expect(days).toBe(-1);
  });

  it('returns -1 for empty activities array', () => {
    expect(daysSinceLastActivity([], 'fungicide')).toBe(-1);
  });

  it('returns -1 for null activities', () => {
    expect(daysSinceLastActivity(null, 'fungicide')).toBe(-1);
  });

  it('is case-insensitive for activity type', () => {
    const days = daysSinceLastActivity(activities, 'FUNGICIDE');
    expect(days).toBe(1);
  });
});

describe('fungicideOverdue', () => {
  it('returns true for -1 (never sprayed)', () => {
    expect(fungicideOverdue(-1)).toBe(true);
  });

  it('returns true when days > 10', () => {
    expect(fungicideOverdue(15)).toBe(true);
  });

  it('returns false when days <= 10', () => {
    expect(fungicideOverdue(5)).toBe(false);
  });

  it('returns false when exactly 10', () => {
    expect(fungicideOverdue(10)).toBe(false);
  });
});