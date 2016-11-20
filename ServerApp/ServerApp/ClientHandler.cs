using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

namespace ServerApp
{
    class ClientHandler
    {
        private Socket _clientSocket;

        public ClientHandler(Socket clientSocket)
        {
            _clientSocket = clientSocket;
        }

        public void Process()
        {
            StringBuilder builder = new StringBuilder();
            int bytes = 0;
            byte[] data = new byte[256];
            int i = 1;

            try
            {
                while (_clientSocket.Connected)
                {
                    if (builder.Length != 0)
                    {
                        builder.Clear();
                    }

                    do
                    {
                        bytes = _clientSocket.Receive(data);
                        builder.Append(Encoding.ASCII.GetString(data, 0, bytes));
                    }
                    while (_clientSocket.Available > 0);

                    Console.WriteLine(DateTime.Now.ToShortTimeString() + ": " + builder.ToString());
                    string message = "Message received" + i++;
                    data = Encoding.ASCII.GetBytes(message);
                    _clientSocket.Send(data);
                }
            }
            catch(SocketException ex)
            {
                Console.WriteLine(ex.Message);
            }
            finally
            {
                _clientSocket.Close();
            }
        }

    }
}
