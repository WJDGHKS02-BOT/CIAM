// 데이터 저장 함수
function setItemWithExpiry(key, value) {
  const expiryTime = new Date().getTime() + (60 * 60 * 1000); // 현재시간 + 60분
  const item = {
    value: value,
    expiry: expiryTime
  };
  localStorage.setItem(key, JSON.stringify(item));
}

// 데이터 가져오기 함수
function getItemWithExpiry(key) {
  const item = localStorage.getItem(key);
  if (!item) return null;

  const parsedItem = JSON.parse(item);
  const now = new Date().getTime();

  // 만료시간 체크
  if (now > parsedItem.expiry) {
    localStorage.removeItem(key); // 만료된 데이터 삭제
    return null;
  }

  return parsedItem.value;
}