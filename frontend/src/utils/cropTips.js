const TIPS = {
  marigold: 'Watch powdery mildew and leaf spot in humid weeks; ensure canopy airflow.',
  wheat: 'Monitor rust and aphids during warm, damp spells; avoid late nitrogen.',
  rice: 'Drain fields before fungicide spray; watch blast in prolonged cloud cover.',
  tomato: 'Scout early blight after rain; stake plants for better air circulation.',
  onion: 'Thrips thrive in dry heat; rotate sprays and avoid water stress.',
  cotton: 'Bollworm pressure rises mid-season; align sprays with pest thresholds.',
  sugarcane: 'Red rot risk rises with standing water; manage drainage after heavy rain.',
};

export function cropTip(cropName) {
  if (!cropName) return null;
  const key = cropName.trim().toLowerCase();
  for (const [name, tip] of Object.entries(TIPS)) {
    if (key.includes(name)) return tip;
  }
  return 'Log sprays and weather regularly so the advisory engine can flag disease risk early.';
}
