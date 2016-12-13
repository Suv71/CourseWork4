using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

namespace ServerApp
{
    public class ClientHandler
    {
        private RadioServer _server;
        private Socket _clientSocket;
        public int Id { get; private set; }


        public ClientHandler(Socket clientSocket, RadioServer server)
        {
            Random rand = new Random();
            _clientSocket = clientSocket;
            Id = rand.Next(1,65000);
            _server = server;
            server.AddClient(this);
        } 

        public void Process()
        {
            byte[] temp;

            try
            {
                while (true)
                {
                    temp = GetMessage();

                    if(temp.Length == 0)
                    {
                        break;
                    }
                    else
                    {
                        Console.WriteLine(Id + " Количество принятых и отправляемых байт = " + temp.Length);
                        _server.SendMessageToClient(Id, temp);
                    }
                }
            }
            catch (SocketException ex)
            {
                Console.WriteLine(ex.Message);
            }
            finally
            {
                Console.WriteLine("Клиент минус: " + Id);
                _server.RemoveClient(Id);
                _clientSocket.Shutdown(SocketShutdown.Both);
                _clientSocket.Close();
            }
        }

        public void SendMessage(byte[] message)
        {
            _clientSocket.Send(message);
        }

        public byte[] GetMessage()
        {
            byte[] data = new byte[1024 * 20];
            int bytes = 0;

            bytes = _clientSocket.Receive(data);
            

            if(bytes < 1024 * 20)
            {
                byte[] result = new byte[bytes];
                for (int i = 0; i < bytes; i++)
                {
                    result[i] = data[i];
                }
                return result;
            }
            
            return data;
        }

    }
}
