package agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BookBuyerAgent extends Agent {

    private String agentNickname = "";
    private String targetBookTitle = "";
    private AID[] sellerAgents = {new AID("seller1", AID.ISLOCALNAME), new AID("seller2", AID.ISLOCALNAME)};


    /*
        Agent Initialization
     */
    protected void setup(){

        int pos = getAID().getName().indexOf("@");
        agentNickname = getAID().getName().substring(0, pos);
        System.out.println("Buyer-agent " + agentNickname + " has started!");

        // Get the title of the book to buy as a start-up argument
        Object[] args = getArguments();
        if(args != null && args.length > 0){

            targetBookTitle = (String) args[0];
            System.out.println(agentNickname + " is trying to buy the book: " + targetBookTitle + ".");

            addBehaviour(new TickerBehaviour(this, 5000) {
                @Override
                protected void onTick() {

//                    myAgent.addBehaviour(new RequestPerformer());
                    System.out.println("myAgent.addBehaviour(new RequestPerformer());");
                }
            });
        }
        else{

            System.out.print("No book title specified. ");
            doDelete();
        }

    }


    /*
        Agent Termination
     */
    protected void takeDown(){

        System.out.println("Buyer-agent " + agentNickname + " has terminated!");
    }


    /*
        Inner Class. Used to exchange messages to request books
     */
    private class RequestPerformer extends Behaviour {

        private AID cheapestSeller;
        private int cheapestPrice;
        private int repliesCounter = 0;
        private MessageTemplate msgTemp;
        private int step = 0;


        public void action(){

            switch(step){

                case 0:     // Send CFP to all sellers

                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for(int i = 0; i < sellerAgents.length; i++){

                        cfp.addReceiver(sellerAgents[i]);
                    }
                    cfp.setContent(targetBookTitle);
                    cfp.setConversationId("book-trade");
                    cfp.setReplyWith("cfp" + System.currentTimeMillis()); // To ensure unique values
                    myAgent.send(cfp);
                    msgTemp = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));

                    step = 1;
                    break;

                case 1:     // Receive all answers from sellers

                    ACLMessage reply = myAgent.receive(msgTemp);

                    if(reply != null){

                        if(reply.getPerformative() == ACLMessage.PROPOSE){

                            int bookPrice = Integer.parseInt(reply.getContent());
                            if(cheapestSeller == null || bookPrice < cheapestPrice){

                                cheapestSeller = reply.getSender();
                                cheapestPrice = bookPrice;
                            }

                            repliesCounter++;
                            if(repliesCounter >= sellerAgents.length){

                                step = 2;
                            }
                        }

                    }
                    else{
                        block();
                    }
                    break;

                case 2:     // Send purchase order to the cheapest seller

                    ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    order.addReceiver(cheapestSeller);
                    order.setContent(targetBookTitle);
                    order.setConversationId("book-trade");
                    order.setReplyWith("order" + System.currentTimeMillis());
                    myAgent.send(order);
                    msgTemp = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
                            MessageTemplate.MatchInReplyTo(order.getReplyWith()));

                    step = 3;
                    break;

                case 3:     // Receive purchase order

                    reply = myAgent.receive(msgTemp);

                    if(reply != null){

                        if(reply.getPerformative() == ACLMessage.INFORM){

                            System.out.println(targetBookTitle + " has been bought for " + cheapestPrice);
                            myAgent.doDelete();
                        }

                        step = 4;
                    }
                    else{

                        block();
                    }
                    break;

            }
        }


        public boolean done(){

            return ((step == 2 && cheapestSeller == null) || step == 4);
        }
    }
}
