export function randomEmail() {
  const rand = Math.random().toString(36).substring(2, 8);
  const timestamp = Date.now();
  return `user_${__VU}_${__ITER}_${rand}_${timestamp}@mail.com`;
}

export function randomPassword() {
  const chars = 'abcdefghijklmnopqrstuvwxyz0123456789';
  return Array.from({ length: 10 }, () => chars[Math.floor(Math.random() * chars.length)]).join('');
}

export function randomName() {
  const names = ['Lucas', 'Sofia', 'Martina', 'Diego', 'Valentina', 'Mateo'];
  const lastNames = ['Perez', 'Gomez', 'Lopez', 'Diaz', 'Ruiz'];
  return `${names[Math.floor(Math.random() * names.length)]} ${lastNames[Math.floor(Math.random() * lastNames.length)]}`;
}
