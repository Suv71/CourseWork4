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
            //StringBuilder builder = new StringBuilder();
            byte[] temp;
           
            try
            {
                while (true)
                {
                    temp = GetMessage();
                    //builder.Append(GetMessage());
                    if(temp.Length == 0)
                    {
                        break;
                    }
                    else
                    {
                        Console.WriteLine(Id + " Количество байт = " + temp.Length);
                        _server.SendMessageToClient(Id, temp);
                        //temp = null;
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
                _clientSocket.Close();
            }
        }

        public void SendMessage(byte[] message)
        {
            //byte[] data = new byte[256];
            //data = Encoding.ASCII.GetBytes(message);
            _clientSocket.Send(message);
        }

        public byte[] GetMessage()
        {
            //StringBuilder builder = new StringBuilder();
            byte[] data = new byte[1024 * 15];
            int bytes = 0;

            do
            {
                bytes += _clientSocket.Receive(data);
                //builder.Append(Encoding.ASCII.GetString(data, 0, bytes));
            }
            while (_clientSocket.Available > 0);

            Console.WriteLine("Количество считанных байтов = " + bytes);
            if(bytes < 1024 * 15)
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
