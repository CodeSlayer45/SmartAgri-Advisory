import { describe, it, expect } from 'vitest';
import { cropTip } from '../utils/cropTips';

describe('cropTip', () => {
  it('returns wheat tip for "Wheat"', () => {
    const tip = cropTip('Wheat');
    expect(tip).toContain('rust');
    expect(tip).toContain('aphids');
  });

  it('returns rice tip for "Rice"', () => {
    const tip = cropTip('Rice');
    expect(tip).toContain('blast');
  });

  it('returns tomato tip for "Tomato"', () => {
    const tip = cropTip('Tomato');
    expect(tip).toContain('early blight');
  });

  it('returns default tip for unknown crop', () => {
    const tip = cropTip('UnknownCrop123');
    expect(tip).toContain('Log sprays');
  });

  it('returns null for null input', () => {
    expect(cropTip(null)).toBeNull();
  });

  it('returns null for empty string', () => {
    expect(cropTip('')).toBeNull();
  });

  it('is case-insensitive', () => {
    const lower = cropTip('wheat');
    const upper = cropTip('WHEAT');
    expect(lower).toBe(upper);
  });

  it('matches partial crop names', () => {
    const tip = cropTip('Sugarcane variety X');
    expect(tip).toContain('Red rot');
  });
});