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
        /*public static int messageToClient = 1;
        public static int newClient = 2;
        public static int clientOut = 3;
        public static int connect = 4;
        public static int disconnect = 5;
        public static int activeClients = 6;*/

        private RadioServer _server;
        private Socket _clientSocket;
        public int Id { get; private set; }


        public ClientHandler(Socket clientSocket, RadioServer server)
        {
            Random rand = new Random();
            _clientSocket = clientSocket;
            //Id = rand.Next(1,65000);
            _server = server;
            if (_server._clients.Count == 0)
            {
                Id = 1;
            }
            else
            {
                Id = _server._clients.Last().Id + 1;
            }
            server.AddClient(this);
        } 

        public void Process()
        {
            byte[] commandBuf;
            int command = 0;
            try
            {
                while (true)
                {
                    commandBuf = GetMessage(4);

                    command = BitConverter.ToInt32(commandBuf, 0);
                    if (command == 0)
                    {
                        break;
                    }
                    else
                    {
                        Console.WriteLine("Полученное command = " + command);
                        HandleCommand(command);
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

        private void HandleCommand(int command)
        {
            byte[] temp;

            switch(command)
            {
                case Commands.messageToClient:
                    {
                        int id = BitConverter.ToInt32(GetMessage(4), 0);
                        Console.WriteLine("Полученное id = " + id);
                        int fileSize = BitConverter.ToInt32(GetMessage(4), 0);
                        Console.WriteLine("Полученное fileSize = " + fileSize);

                        _server.SendMessageToClient(id, BitConverter.GetBytes(Commands.messageToClient));
                        _server.SendMessageToClient(id, BitConverter.GetBytes(Id));
                        _server.SendMessageToClient(id, BitConverter.GetBytes(fileSize));
                        
                        int count = 0;
                        do
                        {
                            temp = GetMessage(0);
                            count += temp.Length;
                            _server.SendMessageToClient(id, temp);
                        } while (count < fileSize);
                        Console.WriteLine("Команда на отправку отработала");
                        break;
                    }

                case Commands.newClient:
                    {

                        break;
                    }

                case Commands.clientOut:
                    {

                        break;
                    }

                case Commands.connect:
                    {
                        int nickSize = BitConverter.ToInt32(GetMessage(4), 0);
                        Console.WriteLine("nickSize = " + nickSize);
                        temp = GetMessage(nickSize);

                        Console.WriteLine("Клиент законнектился " + Encoding.UTF8.GetString(temp));
                        _server.SendAllNewClient(Id, temp);
                        Console.WriteLine("Комманда коннект отработала");
                        break;
                    }


                case Commands.disconnect:
                    {
                        Console.WriteLine("Disconnect принят");
                        _server.SendAllClientOut(Id);


                        /*Console.WriteLine("Клиент минус: " + Id);
                        _server.RemoveClient(Id);
                        _clientSocket.Shutdown(SocketShutdown.Both);
                        _clientSocket.Close();*/

                        break;
                    }

                case Commands.activeClients:
                    {

                        break;
                    }

                default:
                    break;
            }
        }

        public void SendMessage(byte[] message)
        {
            _clientSocket.Send(message);
            Console.WriteLine("Отправил байт " + message.Length);
        }

        public byte[] GetMessage(int byteNumber)
        {
            byte[] data;
            if(byteNumber == 0)
            {
                data = new byte[1024 * 20];
                int bytes = 0;

                bytes = _clientSocket.Receive(data);


                if (bytes < 1024 * 20)
                {
                    byte[] result = new byte[bytes];
                    for (int i = 0; i < bytes; i++)
                    {
                        result[i] = data[i];
                    }
                    Console.WriteLine("Принял байт " + result.Length);
                    return result;
                }
            }
            else
            {
                data = new byte[byteNumber];
                _clientSocket.Receive(data, byteNumber, SocketFlags.None);
            }
            
            
            return data;
        }

    }
}
