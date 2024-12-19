export interface CreateTypo {
  weight: number;
  sizes: [number, number][];
}

export interface FontStyle {
  [key: string]: [string, { lineHeight: string; fontWeight: number }];
}

export const createTypo = ({ weight, sizes }: CreateTypo): FontStyle => {
  return sizes.reduce((acc: FontStyle, [size, lineHeight]) => {
    acc[`${weight}-${size}`] = [`${size}px`, { lineHeight: `${lineHeight}px`, fontWeight: weight }];
    return acc;
  }, {});
};

export default createTypo;
