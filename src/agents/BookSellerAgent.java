package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;

public class BookSellerAgent extends Agent {

    private String agentNickname = "";
    private Hashtable catalogue;
    private BookSellerGui myGui;

    /*
        Agent Initialization
     */
    protected void setup(){

        int pos = getAID().getName().indexOf("@");
        agentNickname = getAID().getName().substring(0, pos);
        System.out.println("Seller-agent " + agentNickname + " has started!");

        catalogue = new Hashtable();

        // GUI stuff
        myGui = new BookSellerGui(this);
        myGui.show();

        // Registration in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("book-selling");
        sd.setName("JADE-book-trading");
        dfd.addServices(sd);
        try{

            DFService.register(this, dfd);
        }
        catch(FIPAException fe){

            fe.printStackTrace();
        }

        // Adding the behaviours for the buyer agents
        addBehaviour(new OfferRequestsServer());
        addBehaviour(new PurchaseOrdersServer());
    }


    /*
        Agent Termination
     */
    protected void takeDown(){

        // Removing the registration in the yellow pages
        try{

            DFService.deregister(this);
        }
        catch(FIPAException fe){

            fe.printStackTrace();
        }

        // GUI stuff
        myGui.dispose();

        System.out.println("Seller-agent " + agentNickname + " has terminated!");
    }


    /*
        Updates the seller catalogue
     */
    public void updateCatalogue(final String bookTitle, final int price){

        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {

                catalogue.put(bookTitle, price);
                System.out.println("Seller-agent has added " + bookTitle + " (" + price + "€) to its catalogue!");
            }
        });
    }


    /*
        Inner Class. Used to exchange messages for selling purposes
     */
    private class OfferRequestsServer extends CyclicBehaviour {

        public void action(){

            // Ignore all messages, except CFP messages
            MessageTemplate msgTemp = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(msgTemp);
            if(msg != null){

                String bookTitle = msg.getContent();
                ACLMessage reply = msg.createReply();
                Integer bookPrice = (Integer) catalogue.get(bookTitle);

                if(bookPrice != null){

                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(bookPrice.intValue()));
                }
                else {

                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                }

                myAgent.send(reply);
            }
            else {

                block();        // For CPU consumption reduction
            }
        }
    }


    /*
        Inner Class. Used to exchange messages for buying purposes
     */
    private class PurchaseOrdersServer extends CyclicBehaviour{

        public void action(){

            MessageTemplate msgTemp = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(msgTemp);
            if(msg != null){

                String bookTitle = msg.getContent();
                ACLMessage reply = msg.createReply();
                Integer bookPrice = (Integer) catalogue.remove(bookTitle);

                if(bookPrice != null){

                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent(String.valueOf(bookPrice.intValue()));
                }
                else{

                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("not-available");
                }

                myAgent.send(reply);
            }
            else{

                block();
            }
        }
    }
}
