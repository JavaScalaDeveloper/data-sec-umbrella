declare module 'jsencrypt' {
  export default class JSEncrypt {
    constructor();
    setPublicKey(key: string): void;
    setPrivateKey(key: string): void;
    encrypt(str: string): string | false;
    decrypt(str: string): string | false;
  }
}
