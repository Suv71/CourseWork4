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
        public String Nickname { get; private set; }


        public ClientHandler(Socket clientSocket, RadioServer server)
        {
            _clientSocket = clientSocket;
            _server = server;

            if (_server.GetClients().Count == 0)
            {
                Id = 1;
            }
            else
            {
                Id = _server.GetClients().Last().Id + 1;
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
                        Console.WriteLine(new StringBuilder("Команда = " + command + " принята на обработку").ToString());
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
                Console.WriteLine(new StringBuilder("Клиент c id = " + Id + " отключился от сервера").ToString());
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
                        int nickSize = BitConverter.ToInt32(GetMessage(4), 0);

                        String nickname = Encoding.UTF8.GetString(GetMessage(nickSize));

                        int fileSize = BitConverter.ToInt32(GetMessage(4), 0);

                        _server.SendMessageToClient(nickname, BitConverter.GetBytes(Commands.messageToClient));
                        _server.SendMessageToClient(nickname, BitConverter.GetBytes(Nickname.Length));
                        _server.SendMessageToClient(nickname, Encoding.UTF8.GetBytes(Nickname));
                        _server.SendMessageToClient(nickname, BitConverter.GetBytes(fileSize));
                        
                        int count = 0;
                        do
                        {
                            temp = GetMessage(0);
                            count += temp.Length;
                            _server.SendMessageToClient(nickname, temp);
                        } while (count < fileSize);

                        break;
                    }

                case Commands.connect:
                    {
                        int nickSize = BitConverter.ToInt32(GetMessage(4), 0);
                        temp = GetMessage(nickSize);

                        Nickname = Encoding.UTF8.GetString(temp);

                        Console.WriteLine(new StringBuilder("Клиент " + Nickname + " вошел в чат").ToString());

                        _server.SendAllNewClient(Id, temp);

                        _server.AddClientInChat();

                        _server.SendActiveClients(Nickname);

                        break;
                    }


                case Commands.disconnect:
                    {
                        Console.WriteLine(new StringBuilder("Клиент " + Nickname + " покидает чат").ToString());

                        _server.SendAllClientOut(Id, Encoding.UTF8.GetBytes(Nickname));

                        _server.RemoveClientFromChat();

                        break;
                    }

                default:
                    break;
            }   
        }

        public void SendMessage(byte[] message)
        {
            _clientSocket.Send(message);
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
