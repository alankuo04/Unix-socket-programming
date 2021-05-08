# Unix-socket-programming
---
+ 本次作業分為 Server 和 Client 兩個程式，一開始先撰寫簡單的傳送字串，確認接正確無誤後再加入傳送檔案之功能
  + Step 1
  	+ Client: 依照投影片中方法，先建立一個 socket，三個參數分別為 AF_INET, SOCK_STREAM, IPPROTO_TCP。在輸入要連接的 IP 和 PORT 後，呼叫 connect 函數並連接上 server。
  	+ Server: 如同 client 端方式先建立 socket 後，呼叫 bind, listen 和 accept 來等待並和 client 端連上線。
  	+ 之後，便在 client 端透過一個 while 迴圈不斷發送一組字串，確認可以將資訊從 client 端發送至 server 端。

  + Step 2	
  	+ Client: 將原先傳送字串的行為改為讀檔及傳送字串
  	+ Server: 將原先接收字串改為接收字串後再將接收到的內容寫入檔案內

  + Step 3
  	+ 因檔案壓縮方法較為複雜，因此並沒有直接加入到 client 和 server 兩個程式中。本次作業我將壓縮編碼的部分拆開至 coded.h 和 coded.cpp 中，先訂下 huffman coding 及 fixed length coding 兩種壓縮方法的介面後，再經由測試確定功能正確。
  + Step 4
  	+ 將獨立開的壓縮編碼透過 include 的方式來在 client 和 server 兩個程式中使用，並加入可以選擇使用哪一種編碼來壓縮，經測試 txt, jpg, odt 三種檔案皆可成功傳送。

+ update: 2021/4/11
