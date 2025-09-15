#!/usr/bin/env python3
# Usage: python3 tcp_client.py SERVER_IP PORT

import socket, sys, threading

def receiver(sock):
    try:
        while True:
            data = sock.recv(4096)
            if not data:
                print("\n[connection closed by remote]")
                break
            # Print received data without interfering with user prompt
            sys.stdout.write("\r" + data.decode(errors='replace') + "\n> ")
            sys.stdout.flush()
    except Exception as e:
        print("\n[receiver error]", e)
    finally:
        try:
            sock.shutdown(socket.SHUT_RD)
        except:
            pass

def main():
    if len(sys.argv) != 3:
        print("Usage: python3 tcp_client.py SERVER_IP PORT")
        return

    host = sys.argv[1]
    port = int(sys.argv[2])

    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
        sock.connect((host, port))
    except Exception as e:
        print("Failed to connect:", e)
        return

    print(f"Connected to {host}:{port}. Type messages and press Enter. Type /quit to exit.")
    t = threading.Thread(target=receiver, args=(sock,), daemon=True)
    t.start()

    try:
        while True:
            msg = input("> ")
            if msg.strip().lower() in ("/quit", "/exit"):
                break
            # send with newline so many servers reply per-line
            sock.sendall((msg + "\n").encode())
    except KeyboardInterrupt:
        print("\nInterrupted by user.")
    finally:
        try:
            sock.shutdown(socket.SHUT_WR)
        except:
            pass
        sock.close()
        print("Connection closed.")

if __name__ == "__main__":
    main()

